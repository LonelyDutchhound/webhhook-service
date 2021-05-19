package com.LonelyDutchhound
package webhooks.db.model

import doobie.implicits.toSqlInterpolator
import doobie.{Put, Update, Write}
import doobie.util.fragment.Fragment
import doobie.util.fragments.{set, values}

import java.sql.Timestamp
import java.util.UUID

case class Column(name: String, refs: Option[ModelDb[_]] = None) {
  override def toString: String = name
}

object Column {
  implicit def fromString(c: String): Column = Column(c)
}


trait ModelDb[T] {

  case class Materialize(system: SystemModelDb, data: T)

  def dataColumn: List[Column]

  def tableName: String

  def getIdColumn: String =  SystemModelDb.dataColumn.head.name

  def tableNameFR: Fragment = Fragment.const(tableName)

  def columns: Seq[Column] = SystemModelDb.dataColumn ++ dataColumn

  def getColumnsFR(name: String): Fragment = Fragment.const(s"$tableName.$name")

  def getWherePart[TT: Put : Write](columnName: String, operation: String, value: TT): Fragment =
    getColumnsFR(columnName) ++ Fragment.const(operation) ++ fr"$value"

  def columnsAsString: String = columns.map(_.toString).mkString(", ")

  def dataColumnsAsString: String = dataColumn.map(_.toString).mkString(", ")

  def select: Fragment =
    Fragment.const(s"SELECT $columnsAsString FROM (SELECT $columnsAsString FROM $tableName WHERE is_delete != 1)  $tableName  ")

  def selectId: Fragment =
    Fragment.const(s"SELECT $getIdColumn FROM (SELECT $columnsAsString FROM $tableName WHERE is_delete != 1)  $tableName  ")

  def insert[TT <: T : Write](data: TT): doobie.Update0 =
    (Fragment.const(s"INSERT INTO $tableName ($dataColumnsAsString)  VALUES (") ++ values(data) ++ fr")").update

  def insertMany[TT <: T : Write](data: List[TT]): Update[TT] = {
    val sql = Fragment.const(s"INSERT INTO $tableName ($dataColumnsAsString)  VALUES (") ++ values(data.head) ++ fr")"
    Update[TT](sql.update.sql)
  }

  def delete(id: UUID): Fragment = {
    val ts = new Timestamp(System.currentTimeMillis())
    val f = set(fr"is_delete=1", fr"dt_update=$ts", fr"dt_delete=$ts")
    Fragment.const(s"UPDATE $tableName") ++ f ++ fr"where id = $id"
  }

}

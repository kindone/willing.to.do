package models

import scala.annotation.implicitNotFound

import models.ActiveRecord._
import net.fwbrasil.activate.migration.Migration
import play.Logger
import net.fwbrasil.activate.storage.relational.idiom.{ h2Dialect, postgresqlDialect }

class CreateInitialTablesMigration extends Migration {
	def timestamp = 201501222101L

	def up = {
		removeAllEntitiesTables
			.ifExists
		Logger.info("Table up")

		createTableForAllEntities
			.ifNotExists
		createInexistentColumnsForAllEntities
		createReferencesForAllEntities
			.ifNotExists
	}
}

class AddIndexToSequencer extends Migration {
	def timestamp = 201501222201L

	def up = {
		table[Sequence]
			.addIndex("nameidx")("name")
			.ifNotExists
	}
}

class DevMigration extends ManualMigration {

	def up = {
		createTableForAllEntities
			.ifNotExists
		createInexistentColumnsForAllEntities
		createReferencesForAllEntities
			.ifNotExists
	}
}



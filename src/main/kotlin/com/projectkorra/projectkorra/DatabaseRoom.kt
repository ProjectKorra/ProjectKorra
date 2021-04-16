package com.projectkorra.projectkorra

import com.projectkorra.projectkorra.DatabaseRoom
import java.util.UUID
import java.sql.ResultSet
import com.projectkorra.projectkorra.storage.DBConnection
import org.bukkit.OfflinePlayer
import java.lang.Error
import java.sql.SQLException

object DatabaseRoom {
    fun getPlayerLevel(player: OfflinePlayer): Int {
        if (!isOpened) {
            throw Error("Connection with DB wasn't estabilished")
        }
        val uuid = player.uniqueId
        val query = "SELECT level FROM pk_players WHERE uuid = '$uuid'"
        val resultSet = DBConnection.sql.readQuery(query)
        try {
            if (resultSet.next()) {
                return resultSet.getInt("level")
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        // Stands for error in request
        return -1
    }

    val isOpened: Boolean
        get() = DBConnection.isOpen()
}
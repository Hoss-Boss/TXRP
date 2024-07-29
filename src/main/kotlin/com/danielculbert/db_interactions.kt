package com.danielculbert

import org.xrpl.xrpl4j.crypto.keys.KeyPair
import java.sql.Connection
import java.sql.DriverManager


fun getConnection(): Connection {
    val documentsPath = getDocumentsFolderPath()
    val wallets_db_file_path = "$documentsPath/TXRP/wallets.db"
    val url = "jdbc:sqlite:$wallets_db_file_path"
    return DriverManager.getConnection(url)
}


fun create_table(connection: Connection) {
    val sql = """
        CREATE TABLE IF NOT EXISTS Wallets (
            ID INTEGER PRIMARY KEY AUTOINCREMENT,
            Name TEXT NOT NULL,
            PrivateKey TEXT NOT NULL,
            PublicKey TEXT NOT NULL,
            Address TEXT NOT NULL,
            xAddress TEXT NOT NULL,
            isEncrypted INTEGER NOT NULL,
            FamilySeed TEXT NOT NULL
        );
    """.trimIndent()

    connection.createStatement().use { stmt ->
        stmt.execute(sql)
    }
}


fun insert_wallet(
    connection: Connection,
    name: String,
    privateKey: String,
    publicKey: String,
    address: String,
    xAddress: String,
    isEncrypted: Int,
    FamilySeed: String
) {
    val sql = """
        INSERT INTO Wallets (Name, PrivateKey, PublicKey, Address, xAddress, isEncrypted, FamilySeed)
        VALUES (?, ?, ?, ?, ?, ?,?);
    """.trimIndent()

    connection.prepareStatement(sql).use { pstmt ->
        pstmt.setString(1, name)
        pstmt.setString(2, privateKey)
        pstmt.setString(3, publicKey)
        pstmt.setString(4, address)
        pstmt.setString(5, xAddress)
        pstmt.setInt(6, isEncrypted)
        pstmt.setObject(7, FamilySeed)
        pstmt.executeUpdate()
    }
}


fun get_all_wallets(connection: Connection): List<Wallet> {
    val sql = "SELECT * FROM Wallets"
    val wallets = mutableListOf<Wallet>()

    connection.createStatement().use { stmt ->
        val rs = stmt.executeQuery(sql)
        while (rs.next()) {
            val wallet = Wallet(
                id = rs.getInt("ID"),
                name = rs.getString("Name"),
                privateKey = rs.getString("PrivateKey"),
                publicKey = rs.getString("PublicKey"),
                address = rs.getString("Address"),
                xAddress = rs.getString("xAddress"),
                isEncrypted = rs.getInt("isEncrypted"),
                familySeed = rs.getString("FamilySeed")
            )
            wallets.add(wallet)
        }
    }

    return wallets
}

fun get_wallet_by_id(connection: Connection, id: Int): Wallet? {
    val sql = "SELECT * FROM Wallets WHERE ID = ?"
    connection.prepareStatement(sql).use { pstmt ->
        pstmt.setInt(1, id)
        val rs = pstmt.executeQuery()
        if (rs.next()) {
            return Wallet(
                id = rs.getInt("ID"),
                name = rs.getString("Name"),
                privateKey = rs.getString("PrivateKey"),
                publicKey = rs.getString("PublicKey"),
                address = rs.getString("Address"),
                xAddress = rs.getString("xAddress"),
                isEncrypted = rs.getInt("isEncrypted"),
                familySeed = rs.getString("FamilySeed")
            )

        }
    }
    return null
}




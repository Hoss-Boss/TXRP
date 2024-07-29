package com.danielculbert

import java.io.File

fun getDocumentsFolderPath(): String {
    val osName = System.getProperty("os.name").lowercase()
    val userHome = System.getProperty("user.home")

    return when {
        "win" in osName -> "$userHome\\Documents"
        "mac" in osName -> "$userHome/Documents"
        "nux" in osName -> "$userHome/Documents"
        else -> userHome
    }
}

fun create_existence_file(filename: String): File {
    val documentsPath = getDocumentsFolderPath()
    val helloXRPWalletPath = "$documentsPath/TXRP" // Path for the new folder

    // Create a File object for the new Hello-XRP-Wallet directory
    val helloXRPWalletDir = File(helloXRPWalletPath)

    // Ensure the Hello-XRP-Wallet directory exists
    if (!helloXRPWalletDir.exists()) {
        helloXRPWalletDir.mkdirs() // Make directories if they do not exist
    }

    // Create a file in the Hello-XRP-Wallet directory
    val file = File(helloXRPWalletDir, filename)
    file.createNewFile() // Creates the file if it does not exist

    return file
}

fun check_if_db_file_exists(): Boolean {

    val helloXRPWalletPath = "${getDocumentsFolderPath()}/TXRP"
    val db_file = File("${helloXRPWalletPath}/wallets.db")

    if (db_file.exists()) {
        println("File exists!")
        return true
    }
    else {
        println("File does not exist!")
        return false
    }

}

fun create_file(filename: String): File {
    val documentsPath = getDocumentsFolderPath()
    val helloXRPWalletPath = "$documentsPath/TXRP" // Path for the new folder

    // Create a File object for the new Hello-XRP-Wallet directory
    val helloXRPWalletDir = File(helloXRPWalletPath)

    // Ensure the Hello-XRP-Wallet directory exists
    if (!helloXRPWalletDir.exists()) {
        helloXRPWalletDir.mkdirs() // Make directories if they do not exist
    }

    // Create a file in the Hello-XRP-Wallet directory
    val file = File(helloXRPWalletDir, filename)
    file.createNewFile() // Creates the file if it does not exist

    return file
}

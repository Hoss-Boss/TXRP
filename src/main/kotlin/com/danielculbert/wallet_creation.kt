package com.danielculbert

import okhttp3.HttpUrl.Companion.toHttpUrl
import org.xrpl.xrpl4j.client.XrplClient
import org.xrpl.xrpl4j.codec.addresses.AddressCodec
import org.xrpl.xrpl4j.codec.addresses.Base58
import org.xrpl.xrpl4j.crypto.keys.KeyPair
import org.xrpl.xrpl4j.crypto.keys.Seed
import org.xrpl.xrpl4j.model.transactions.Address
import kotlin.random.Random
import java.security.SecureRandom
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.xrpl.xrpl4j.crypto.keys.Base58EncodedSecret
import org.xrpl.xrpl4j.crypto.keys.Passphrase
import java.security.KeyPairGenerator
import java.security.Security
import org.xrpl.xrpl4j.crypto.keys.ImmutableKeyPair
import org.xrpl.xrpl4j.crypto.keys.bc.BcKeyUtils

var created_address = ""

var is_encrypted = 0
var name = "..."
var id = Random.nextInt()

data class Wallet(
    val id: Int,
    val name: String,
    val privateKey: String,
    val publicKey: String,
    val address: String,
    val xAddress: String,
    val isEncrypted: Int,
    val familySeed: String,
)


fun create_wallet() {


    if (check_if_db_file_exists() == false) {
        create_file("wallets.db")
        val conn = getConnection()
        create_table(conn)
        conn.close()
    }

    println("Wallet information will be stored in Documents/TXRP/wallets.db")
    println("1. Create a wallet without encryption")
    println("2. Create a wallet with encryption")
    println("b. Go back to previous screen")

    while (true) {

        val user_input = readLine()
        if (user_input.equals("1")) {
            create_without_encryption()
            return
            }

        if (user_input.equals("b")) {return}


    }//while



}//create_wallet

fun create_without_encryption() {

    println("Pick a name for the wallet")
    name = readLine()?:"Wallet"
    //val conn = getConnection()
    generate_xrp_wallet(name, 0)
    //conn.close()

}





fun generate_family_seed(): String {
    Security.addProvider(BouncyCastleProvider())
    val random = SecureRandom()
    val seedBytes = ByteArray(16)
    random.nextBytes(seedBytes)

    // Add a version byte (0x21 for family seed)
    val versionByte = byteArrayOf(0x21)
    val versionedSeed = versionByte + seedBytes

    // Calculate checksum
    val checksum = calculateChecksum(versionedSeed)

    // Append checksum to the versioned seed
    val seedWithChecksum = versionedSeed + checksum

    // Encode the final seed
    return Base58.encode(seedWithChecksum)
}

fun calculateChecksum(data: ByteArray): ByteArray {
    val sha256 = java.security.MessageDigest.getInstance("SHA-256", BouncyCastleProvider())
    val hash1 = sha256.digest(data)
    val hash2 = sha256.digest(hash1)
    return hash2.copyOfRange(0, 4)
}


fun generate_KeyPair_from_family_seed(familySeed: String): KeyPair {

    val seed = Seed.fromBase58EncodedSecret(Base58EncodedSecret {familySeed})
    //println("Generating key pair from the family seed $familySeed")
    return seed.deriveKeyPair()

}



fun generate_xrp_wallet(wallet_name: String, encryption: Int) {

    val conn = getConnection()

    val rippledUrl = "https://s2.ripple.com:51234/".toHttpUrl()

    val xrplClient = XrplClient(rippledUrl)

    val familySeed = generate_family_seed()

    val randomKeyPair: KeyPair = generate_KeyPair_from_family_seed(familySeed)

    val classicAddress: Address = randomKeyPair.publicKey().deriveAddress()

    //println("Classic Address: " + classicAddress);

    val xAddress = AddressCodec.getInstance().classicAddressToXAddress(classicAddress, false)
    //println("X Address: " + xAddress);

    val privateKey = randomKeyPair.privateKey().value()


    val publicKey = Base58.encode(randomKeyPair.publicKey().base58Value().toByteArray())
    //println("Public Key: " + publicKey);

    val privateKeyBase58 = Base58.encode(privateKey.toByteArray())
    //println("Private Key (Base58): $privateKeyBase58")

    // val conn = getConnection()
    insert_wallet(
        conn,
        wallet_name,
        privateKeyBase58,
        publicKey.toString(),
        classicAddress.toString(),
        xAddress.toString(),
        encryption,
        familySeed

    )
    conn.close()
    //insertWallet(db_connection, "Wallet",)
    println("Wallet created! Your new XRP Address is: ${classicAddress.toString()}")


}
package com.danielculbert

import com.google.common.primitives.UnsignedInteger
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException
import org.xrpl.xrpl4j.client.XrplClient
import org.xrpl.xrpl4j.crypto.keys.PrivateKey
import org.xrpl.xrpl4j.crypto.signing.SignatureService
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction
import org.xrpl.xrpl4j.crypto.signing.bc.BcSignatureService
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult
import org.xrpl.xrpl4j.model.client.common.LedgerIndex
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier
import org.xrpl.xrpl4j.model.client.fees.FeeResult
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult
import org.xrpl.xrpl4j.model.transactions.Address
import org.xrpl.xrpl4j.model.transactions.Payment
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount
import java.math.BigDecimal


fun search_wallets() {

    clear_screen()



    val conn = getConnection()
    val walletsList = get_all_wallets(conn)


    //println("Select the wallet ID that you want to use for transactions")

    while (true) {
        println("Type b to go back")
        println("Type the ID of the wallet you want to use:")

        for (wallet in walletsList) {
            println("ID: ${wallet.id}, Name: ${wallet.name}, Address: ${wallet.address}")
        }

        val user_input = readLine()
        if (user_input == null || user_input.trim().isEmpty()) {
          println("Type the ID of the wallet you want to use, or type b to go back")
          continue
        }

        if (user_input == "b" || user_input == "B") {
            return
        }

        val user_input_number = user_input.toIntOrNull()

        if (user_input_number == null) {
            println("Input must be an integer")
            continue

        }

        val wallet: Wallet? = get_wallet_by_id(conn, user_input_number)
        if (wallet == null) {
            println("Not a valid ID. Type a valid ID or b to go back.")
            continue
        }
        else {
            choose_activity_once_wallet_is_selected(wallet)
        }

    }



}//search_wallets

fun view_wallet_balance(wallet: Wallet): Float {



    val rippledUrl = "https://s2.ripple.com:51234/".toHttpUrl()
    //println("Constructing an XrplClient connected to $rippledUrl")
    val xrplClient = XrplClient(rippledUrl)

    val address = Address.of(wallet.address)


    try {
        // Look up your Account Info
        val requestParams = AccountInfoRequestParams.of(address)
        val accountInfoResult = xrplClient.accountInfo(requestParams)

        // Print the balance
        val balance = accountInfoResult.accountData().balance()
        val balance_float = (balance.value().toFloat()/1000000) - 10
        println("Balance: ${balance_float} XRP")
        return balance_float
    }//try
    catch (e: JsonRpcClientErrorException) {
        println("Error: Account not found on blockchain. Please fund the address with at least 10 XRP before attempting to view balance or send XRP.")
        return 0.0F
    }
}//view_wallet_balance

fun choose_activity_once_wallet_is_selected(wallet: Wallet) {
    clear_screen()
    println("Your wallet is ${wallet.address}")
    //val address = Address.of("rprtY4gDrHQxGRuWAizfXHZteLM9waL5oK")
    //val Wallet2 = Wallet(234, "dan_real", "asd", "asd", "rprtY4gDrHQxGRuWAizfXHZteLM9waL5oK", "2", 0)
    view_wallet_balance(wallet)
    println("1. Send XRP")
    println("2. View incoming and outgoing transactions from this wallet")
    println("b. Go back")


    while (true) {
        val user_input = readLine()
        if (user_input == null || user_input.trim().isEmpty()) {
            continue
        }
        else {
            if (user_input == "b" || user_input == "B") {return}
            if (user_input == "1")
            {
                begin_send_xrp(wallet)
            }
            if (user_input == "2") {"Pass"}
        }
    }//while true

}//choose_activity_once_wallet_is_selected

fun generate_payment(wallet: Wallet, destination: String, payment_amount: BigDecimal): Payment? {

    val rippledUrl = "https://s2.ripple.com:51234/".toHttpUrl()
    //println("Constructing an XrplClient connected to $rippledUrl")
    val xrplClient = XrplClient(rippledUrl)


    // Prepare transaction --------------------------------------------------------
// Look up your Account Info
    val requestParams: AccountInfoRequestParams = AccountInfoRequestParams.builder()
        .account(Address.of(wallet.address))
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
        .build()
    val accountInfoResult: AccountInfoResult = xrplClient.accountInfo(requestParams)
    val sequence = accountInfoResult.accountData().sequence()


// Request current fee information from rippled
    val feeResult: FeeResult = xrplClient.fee()
    val openLedgerFee = feeResult.drops().openLedgerFee()


// Get the latest validated ledger index
    val validatedLedger: LedgerIndex = xrplClient.ledger(
        LedgerRequestParams.builder()
            .ledgerSpecifier(LedgerSpecifier.VALIDATED)
            .build()
    )
        .ledgerIndex()
        .orElseThrow { RuntimeException("LedgerIndex not available.") }


// LastLedgerSequence is the current ledger index + 4
    val lastLedgerSequence = validatedLedger.plus(UnsignedInteger.valueOf(4)).unsignedIntegerValue()


// Construct a Payment
    val payment: Payment = Payment.builder()
        .account(Address.of(wallet.address))
        .amount(XrpCurrencyAmount.ofXrp(payment_amount))
        .destination(Address.of(destination))
        .sequence(sequence)
        .fee(openLedgerFee)
        .signingPublicKey(generate_KeyPair_from_family_seed(wallet.familySeed).publicKey())
        .lastLedgerSequence(lastLedgerSequence)
        .build()
    //println("Constructed Payment: $payment")

    return payment

}//construct_payment

fun begin_send_xrp(wallet: Wallet) {

    clear_screen()
    println("What is the destination address? Type b to go back.")

    while(true) {
        val user_input = readLine()
        if (user_input == null || user_input.trim().isEmpty()) {continue}

        if (user_input == "b" || user_input == "B") {return}

        try {
            val destination_address = Address.of(user_input)
            ask_for_payment_amount(wallet, user_input)
        }
        catch (e: Exception) {
            println("Not a valid destination address")
            continue
        }



    }//while_true



}//begin_send_xrp

fun ask_for_payment_amount(wallet: Wallet, destination: String) {

    val rippledUrl = "https://s2.ripple.com:51234/".toHttpUrl()
    //println("Constructing an XrplClient connected to $rippledUrl")
    val xrplClient = XrplClient(rippledUrl)


    while(true) {

        clear_screen()
        println("How much XRP do you want to send? Type b to go back")

        val user_input = readLine()
        if (user_input == null || user_input.trim().isEmpty()) {continue}

        if (user_input == "b" || user_input == "B") {return}

        val user_input_BigDecimal = user_input.toBigDecimalOrNull()
        if (user_input_BigDecimal == null) {
            println("Input must be an integer or decimal number")
            continue
        }

        try {
            val payment = generate_payment(wallet, destination, user_input_BigDecimal)


            // Sign transaction -----------------------------------------------------------
            // Construct a SignatureService to sign the Payment
            val signatureService: SignatureService<PrivateKey> = BcSignatureService()


            // Sign the Payment
            val signedPayment: SingleSignedTransaction<Payment> = signatureService.sign(generate_KeyPair_from_family_seed(wallet.familySeed).privateKey(), payment)
            println("Signed Payment: " + signedPayment.signedTransaction())


            // Submit transaction ---------------------------------------------------------
            val paymentSubmitResult: SubmitResult<Payment> = xrplClient.submit(signedPayment)
            //println(paymentSubmitResult)
            println("Success! Note: If you attempted to send more XRP than is in your wallet, only 0.000001 XRP will be sent.")
        }
        catch (e: Exception) {
            println("Error, try again: ${e.message}")
            continue
        }



    }//while true

}






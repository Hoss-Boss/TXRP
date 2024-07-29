package com.danielculbert

import org.xrpl.xrpl4j.crypto.keys.Passphrase
import org.xrpl.xrpl4j.crypto.keys.Seed

fun main() {

    while(true) {

        println("Welcome!")
        println("1. Create New Wallet")
        println("2. Select existing wallet")


        choose_option()

    }//while
}//main

fun choose_option() {

    while (true) {
        val user_input = readLine()

        if (user_input.equals("1")) {
            clear_screen()
            create_wallet()
            return
        }
        if (user_input.equals("2")) {
            clear_screen()
            search_wallets()
            return
        } else  {
            println("Enter a valid input")
            continue

        }
    }//while

}


fun clear_screen() {
    print("\u001b[H\u001b[2J")
    System.out.flush()
}
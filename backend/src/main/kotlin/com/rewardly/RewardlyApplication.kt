package com.rewardly

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RewardlyApplication

fun main(args: Array<String>) {
    runApplication<RewardlyApplication>(*args)
}

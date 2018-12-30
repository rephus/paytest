package paytest.factory

import java.util.UUID

import paytest.model.Account

import scala.util.Random

object AccountFactory {
  def randomString = Random.alphanumeric.take(Random.nextInt(10)).mkString

  def random: Account = {
    Account(
      id = Some(UUID.randomUUID().toString),
      accountName = randomString,
      accountNumber = BigDecimal.apply(Random.nextInt),
      numberCode = randomString,
      accountType = Random.nextInt,
      address = randomString,
      bankId= Random.nextInt,
      bankIdCode = randomString,
      name = randomString
    )
  }
}
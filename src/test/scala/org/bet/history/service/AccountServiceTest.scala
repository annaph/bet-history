package org.bet.history.service

import org.bet.history.model.Account
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Await
import scala.concurrent.duration._

class AccountServiceTest extends AnyFlatSpec with Matchers {

  private val accountService = AccountServiceImpl(inputFilePath = "test-accounts.csv")

  "getAccount" should "return Account" in {
    // given
    val accountId = "acc-0001"

    // when
    val actual = Await.result(accountService getAccount accountId, 3.seconds).toOption.get

    // then
    actual shouldBe Account(
      id = accountId,
      name = "Martin Fowler",
      phoneNumber = "555-0019")
  }

  it should "return service error for non-existing account" in {
    // given
    val nonExistingAccountId = "non-existing"

    // when
    val serviceError = Await.result(accountService getAccount nonExistingAccountId, 3.seconds).swap.toOption.get
    val actual = serviceError.throwable.get

    // then
    actual shouldBe a[AccountNotFoundException.type]
  }

}

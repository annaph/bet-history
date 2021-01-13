package org.bet.history.service

import org.bet.history.model.{Account, AccountServiceError}

import scala.concurrent.Future

trait AccountService {

  def getAccount(accountId: String): Future[Either[AccountServiceError, Account]]

}

class AccountServiceImpl(accounts: List[Account], keySelector: Account => String)
  extends RepositoryService(accounts, keySelector) with AccountService {

  override def getAccount(accountId: String): Future[Either[AccountServiceError, Account]] = {
    lazy val serviceError = AccountServiceError(
      msg = s"No account with ID '$accountId'",
      throwable = Some(AccountNotFoundException))
    getItem(accountId, serviceError)
  }

}

object AccountServiceImpl {

  def apply(inputFilePath: String = "accounts.csv"): AccountServiceImpl =
    new AccountServiceImpl(
      accounts = RepositoryService.readResource(inputFilePath)(parseLine),
      keySelector = account => account.id)

  private def parseLine(line: String): Account =
    line.split(",").map(_.trim) match {
      case Array(id, name, phoneNumber) =>
        Account(id, name, phoneNumber)
      case _ =>
        throw new Exception("Error parsing accounts input file!")
    }

}

case object AccountNotFoundException extends Exception

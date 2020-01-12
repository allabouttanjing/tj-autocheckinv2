package autocheckin.tj

import java.net.URL

import io.appium.java_client.android.{AndroidDriver, AndroidElement}
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.support.ui.WebDriverWait
import org.openqa.selenium.{
  By,
  NoSuchElementException,
  StaleElementReferenceException,
  WebDriver
}

import scala.jdk.CollectionConverters._

object Main extends App with WaitUtil {
  val capabilities = DesiredCapabilities.android()
  capabilities.setCapability("deviceName", "AOSP on IA Emulator")
  capabilities.setCapability("platformName", "Android")
  capabilities.setCapability("platformVersion", "9.0")
  capabilities.setCapability("appPackage", "com.sina.weibo")
  capabilities.setCapability("browserName", "")
  capabilities.setCapability("appActivity", "com.sina.weibo.MainTabActivity")
  capabilities.setCapability("noReset", "true")
  capabilities.setCapability("fullReset", "false")
//  capabilities.setCapability("newCommandTimeout", "600")
  implicit val androidDriver: AndroidDriver[AndroidElement] =
    new AndroidDriver[AndroidElement](new URL("http://127.0.0.1:4723/wd/hub"),
                                      capabilities)

  val me = androidDriver
    .findElement(By.xpath("//android.view.ViewGroup[@content-desc = 'Me']"))
  waitUtil(() => me.click(), driver => {
    driver.findElement(By.id("com.sina.weibo:id/cabFollow")).isDisplayed
  })
  val follow = androidDriver.findElement(By.id("com.sina.weibo:id/cabFollow"))

  waitUtil(() => follow.click(), driver => {
    driver
      .findElement(By.xpath("//android.widget.TextView[@text = 'Super Topic']"))
      .isDisplayed
  })
  val superTopic = androidDriver
    .findElement(By.xpath("//android.widget.TextView[@text = 'Super Topic']"))

  waitUtil(() => superTopic.click(), driver => {
    driver.findElement(By.id("com.sina.weibo:id/rightgrp")).isDisplayed
  })
  val viewAll = androidDriver.findElement(By.id("com.sina.weibo:id/rightgrp"))

  waitUtil(() => viewAll.click(), driver => {
    driver.findElements(By.className("android.widget.Button")).asScala.nonEmpty
  })
  val checkinButtons =
    androidDriver
      .findElements(By.className("android.widget.Button"))
      .asScala
      .filter(b => b.getText.getBytes.startsWith("签到".getBytes))
  checkinButtons.foreach { button =>
    waitUtil(() => button.click(), driver => {
      driver.findElement(By.id("com.sina.weibo:id/ivUserInfoBack")).isDisplayed
    })
    androidDriver.findElement(By.id("com.sina.weibo:id/ivUserInfoBack")).click()

    waitUtil(() => {}, driver => {
      driver
        .findElements(By.className("android.widget.Button"))
        .asScala
        .nonEmpty
    })
  }

  println("[+]done")
  androidDriver.closeApp()
}

trait WaitUtil {
  def waitUtil(
      action: () => Unit,
      predicate: WebDriver => Boolean,
      timeOutInSeconds: Long = 10)(implicit driver: WebDriver): Unit = {
    val wait = new WebDriverWait(driver, timeOutInSeconds)
    var applyAction = true
    wait.until(driver => {
      try {
        if (applyAction)
          action()
        predicate(driver)
      } catch {
        case _: NoSuchElementException         => false
        case _: StaleElementReferenceException =>
          // if it is in the transition, then the neither the initiating element nor the targeting element is visible
          // so we stop the action (e.g. clicking) and just wait
          applyAction = false
          false
      }
    })
  }
}

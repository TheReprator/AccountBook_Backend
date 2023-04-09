package dev.reprator.country

import org.junit.platform.suite.api.SelectPackages
import org.junit.platform.suite.api.Suite
import org.junit.platform.suite.api.SuiteDisplayName

@Suite
@SuiteDisplayName("Country api Unit Test Suite")
@SelectPackages("dev.reprator.country")
class CountryTestSuite
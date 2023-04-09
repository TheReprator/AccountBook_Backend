package dev.reprator.language

import org.junit.platform.suite.api.SelectPackages
import org.junit.platform.suite.api.Suite
import org.junit.platform.suite.api.SuiteDisplayName

@Suite
@SuiteDisplayName("Language api Unit Test Suite")
@SelectPackages("dev.reprator.language")
class LanguageTestSuite
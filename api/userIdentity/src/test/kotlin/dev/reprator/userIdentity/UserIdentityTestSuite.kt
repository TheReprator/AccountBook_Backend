package dev.reprator.userIdentity

import org.junit.platform.suite.api.SelectPackages
import org.junit.platform.suite.api.Suite
import org.junit.platform.suite.api.SuiteDisplayName

@Suite
@SuiteDisplayName("User identity api Unit Test Suite")
@SelectPackages("dev.reprator.userIdentity")
class UserIdentityTestSuite
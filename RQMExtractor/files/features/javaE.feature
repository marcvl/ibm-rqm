Feature: javaE
	Scenario: At my calculator
	Given I enter 8
	And I select add
	And I enter 4
	And I select equals
	Then I should see 12
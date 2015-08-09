Feature: calculator

Scenario: Initial Check Calculator Correct
	Given I enter first argument 5
	And I enter second argument 3
	When I select to "summarize"
	Then results should be 8
	
Scenario: Initial Check Calculator Fail
	Given I enter first argument 5
	And I enter second argument 200
	When I select to "summarize"
	Then results should be 205
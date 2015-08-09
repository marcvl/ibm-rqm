#RQM Extractor#

Description: A supporting program to extract the test case design from the Test Case. With that
information the Cucumber enginge is than started.

See also http://business.vanlint5.nl/html2/cucumber.html

##How to use##


* Copy the files directory in the working directory of the RQM agent.
* Copy the latest RQMExtractor.jar file to the same directory.
* Write supporting features

An example Gherkin feature definition.
<PRE>
Feature: Basic
	Scenario: In the homepage
	Given I am on the homepage
	Then I should see "Welcome"
</PRE>

##Other Sources##
* http://business.vanlint5.nl/html2/cucumber.html
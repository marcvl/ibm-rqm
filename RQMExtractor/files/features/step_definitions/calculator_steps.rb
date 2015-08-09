Given(/^I enter first argument (\d+)$/) do |arg1|
	MyCalc = Calculator.new
	MyCalc.on
	MyCalc.setArg1(arg1.to_i)
  #@argument1=arg1.to_i
end
And(/^I enter second argument (\d+)$/) do |arg2|
	MyCalc.setArg2(arg2.to_i)
end

When(/^I select to (.*?)$/) do |arg1|
	MyCalc.sum
end

Then(/^results should be (\d+)$/) do |givenresult|
	givenresult=givenresult.to_i
	givenresult.should == MyCalc.result.to_i
	# How to kill/drop a Instance
	MyCalc = nil
	MyCalc.inspect
end
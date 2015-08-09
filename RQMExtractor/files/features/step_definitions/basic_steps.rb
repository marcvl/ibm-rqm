Given(/^I am on the homepage$/) do
end

Then(/^I should see "(.*?)"$/) do |arg1|
	arg1.should == "Welcome"
end

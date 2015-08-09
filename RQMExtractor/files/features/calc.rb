#!/usr/bin/ruby

class Calculator
	attr_reader :result, :argument1, :argument2, :dummy
	def initialize()
		puts "Init..."
	end
	def on
		@result="Not set"
		@argument1=0
		@argument2=0
		puts "Calculator is on"
		printResult
	end
	def printResult
		puts "Result: #{@result}"
	end
	
	def setArg1(given1)
		@argument1=given1
		puts "Argument1: #{@argument1}"
	end
	def setArg2(given2)
		@argument2=given2
		puts "Argument2: #{@argument2}"

	end
	def argument2
		@argument2
	end
	def sum
		puts "Function: Summarize"
		if @argument2 > 100
			@argument2 = 500
			puts "*** ERROR infused argument2 set to #{@argument2}"
		end
		@result=@argument1+@argument2
		printResult
	end
end

# Now using above class to create objects
#MyCalc = Calculator.new()
#MyCalc.on
#MyCalc.setArg1(3)
# puts "#{MyCalc.argument1}"
#MyCalc.setArg2(6)
#MyCalc.sum

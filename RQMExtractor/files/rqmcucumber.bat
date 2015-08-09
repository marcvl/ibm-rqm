rem Prepare clean environment
rem
rem Set work directory
set WorkDir=C:\\PRJ\\RQM Extractor
rem Set common directory
set CommonDir=C:\\PRJ\\CMD
set FeatureFile=%qm_RQM_TESTCASE_WEBID%.feature
set LogFile=%qm_RQM_TESTCASE_WEBID%_logfile.txt
set CucumberResult=%qm_RQM_TESTCASE_WEBID%_Cucumber.html
cd %WorkDir%

if exist features goto continue
echo *** ERROR The features directory does not exists in %WorkDir% > %LogFile%
echo *** ERROR RQM Cucumber process aborted >> %LogFile%
set retvalue=999
goto end

:continue
del /Q %FeatureFile%
del /Q %FeatureFile%.txt
del /Q %LogFile%
del /Q %CucumberResult%

echo Starting Cucumber %FeatureFile% > %LogFile%
echo Current date and time >> %LogFile%
date /T >> %LogFile%
time /T >> %LogFile%
echo Current Working directory >> %LogFile%
cd >> %LogFile%

rem Extract Test Case Design
rem
call %CommonDir%\setvars.cmd
echo Extracting Test Case %qm_RQM_TESTCASE_WEBID% >> %LogFile%
java -jar RQMExtractor.jar -id=%qm_RQM_TESTCASE_WEBID% >> %LogFile%

rem Run the Cucumber engine
rem
echo Executing Start: Cucumber %FeatureFile% --format html --out %CucumberResult% >> %LogFile%
call cucumber %FeatureFile% --format html --out %CucumberResult% 2>> %LogFile%
set retvalue=%errorlevel%
echo Executing End: Cucumber >> %LogFile%

:end
rem Set Attachment Files and Exit
rem
rem For easy access in the browser we add a .txt extension to the feature file
ren %FeatureFile% %FeatureFile%.txt
echo %FeatureFile%=%WorkDir%\\%FeatureFile%.txt >> %qm_AttachmentsFile%
echo %LogFile%=%WorkDir%\\%LogFile% >> %qm_AttachmentsFile%
echo %CucumberResult%=%WorkDir%\\%CucumberResult% >> %qm_AttachmentsFile%
rem Do not remove files. These are processed after closure of script!

exit %retvalue%


import hudson.model.*

def defaultConfigure = {
    it / 'properties' / 'jenkins.model.BuildDiscarderProperty' {
        strategy {
            'daysToKeep'('5')
            'numToKeep'('15')
            'artifactDaysToKeep'('-1')
            'artifactNumToKeep'('-1')
        }
    }
    it / 'properties' / 'com.coravy.hudson.plugins.github.GithubProjectProperty' {
        'projectUrl'('https://github.com/ProzorroUKR/robot_tests/')
        displayName()
    }
}

def defaultScm = {
    git {
        remote {
            github("ProzorroUKR/robot_tests", "https")
        }
        branch("\${BRANCH}")
    }
}

def defaultWrappers(boolean xvfb_enable = false, Integer timeoutSeconds = null) {
    return {
        preBuildCleanup {
            includePattern('test_output')
            deleteDirectories(true)
        }
        colorizeOutput('css')
        if (xvfb_enable) {
            xvfb('default') {
                parallelBuild()
                autoDisplayName(true)
            }
        }
        if (timeoutSeconds) {
            timeout {
                noActivity(timeoutSeconds)
                failBuild()
                writeDescription('Build failed due to timeout after {0} minutes')
            }
        }
    }
}

def defaultPublishers = {
    publishRobotFrameworkReports {
        passThreshold(100.0)
        unstableThreshold(75.0)
        onlyCritical()
        outputPath("test_output")
        outputFileName("output.xml")
        reportFileName("report.html")
        logFileName("log.html")
        otherFiles("selenium-screenshot-*.png")
    }
    archiveArtifacts {
        pattern("test_output/output.xml")
        allowEmpty(false)
        onlyIfSuccessful(false)
        fingerprint(false)
        defaultExcludes(true)
    }
    chucknorris()
}

def defaultTriggers(cronTime = null) {
    return {
        if (cronTime) {
            cron(cronTime)
        }
    }
}

def defaultParameters(config) {
    return {
        stringParam("BRANCH", config.branch, "")
        booleanParam("EDR", config.edr, "")
        booleanParam("DFS", config.dfs, "")
        config.params.collect { k,v -> stringParam(k, v, "") }
    }
}

def defaultEnv() {
    return {
    groovy("if (EDR.toBoolean()) {return [EDR_PRE_QUALIFICATION: '-i pre-qualifications_check_by_edrpou', EDR_QUALIFICATION: '-i qualifications_check_by_edrpou']}")
    }
}

String shellBuildout = "python2 bootstrap.py\nbin/buildout -N buildout:always-checkout=force\nbin/develop update -f"
String shellPhantom  = "sed -r -i 's/browser: *(chrome|firefox)/browser:  PhantomJS/gi' op_robot_tests/tests_files/data/users.yaml"
String shellRebot    = "robot_wrapper bin/rebot -o test_output/output.xml -l test_output/log.html -r test_output/report.html -R test_output/*.xml"
String robotWrapper  = "robot_wrapper bin/op_tests --consolecolors on "
String openProcedure = "-o base_output.xml -s openProcedure"
String auction       = "-o auction_output.xml -s auction_full"
String auction_short = "-o auction_short_output.xml -s auction"
String qualification = "-o qualification_output.xml -s qualification"
String contractsign  = "-o contract_output.xml -s contract_signing"
String contractmanagement  = "-o contract_management_output.xml -s contract_management"
String agreement = "-o agreement_output.xml -s agreement"
String selection = "-o selection_output.xml -s selection"
String planning = "-o planning_output.xml -s planning"
String complaints = "-o complaints_output.xml -s complaints_new"
String no_auction = "-v submissionMethodDetails:\"quick(mode:no-auction)\""
String cancellation = "-o cancellation_output.xml -s cancellation"
String accelerate_openeu = "-v BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openeu\":{\"tender\":[1,5], \"accelerator\":14400}}}}"
 
def remoteToken = null
try {
    remoteToken = Thread.currentThread().executable.buildVariableResolver.resolve("TOKEN")
} catch(Exception e) {}

[
        [
                environment: 'k8s',
                params: [
                    "RELEASE_NAME": "main",
                    "API_HOST_URL": "http://api.\${RELEASE_NAME}.k8s.prozorro.gov.ua",
                    "DS_HOST_URL": "http://ds.k8s.prozorro.gov.ua",
                    "DASU_API_HOST_URL": "http://audit.k8s.prozorro.gov.ua",
                    "DASU_API_VERSION": "2.5",
                    "API_VERSION": "2.5",
                    "EDR_VERSION": "0",
                    "AUCTION_REGEXP": "^http?:\\/\\/auctions\\.\${RELEASE_NAME}\\.k8s\\.prozorro\\.gov\\.ua\\/(esco-)?tenders\\/([0-9A-Fa-f]{32})",
                    "DS_REGEXP": "^http?:\\/\\/ds\\.k8s\\.prozorro\\.gov\\.ua\\/get\\/([0-9A-Fa-f]{32})",
                ],
                cron: null,
                branch: "dev_prozorro",
                concurrentBuild: true,
                edr: false,
                dfs: false
        ],
	    [
                environment: 'sandbox_prozorro',
                params: [
                    "API_HOST_URL": "https://lb-api-sandbox.prozorro.gov.ua",
                    "DS_HOST_URL": "https://upload-docs-sandbox.prozorro.gov.ua",
                    "EDR_HOST_URL": "https://lb-edr-sandbox.prozorro.gov.ua",
                    "DASU_API_HOST_URL": "https://audit-api-sandbox.prozorro.gov.ua",
                    "API_VERSION": "2.4",
                    "EDR_VERSION": "0",
                    "AUCTION_REGEXP": "^https?:\\/\\/auction(?:-sandbox)?\\.prozorro\\.gov\\.ua\\/(esco-)?tenders\\/([0-9A-Fa-f]{32})",
                    "DS_REGEXP": "^https?:\\/\\/public-docs(?:-sandbox)?\\.prozorro\\.gov\\.ua\\/get\\/([0-9A-Fa-f]{32})",
                ],
                cron: "H 0 * * *",
                branch: "dev_prozorro",
                concurrentBuild: false,
                edr: true,
                dfs: false
        ],
        [
                environment: 'staging_prozorro',
                params: [
                    "API_HOST_URL": "https://lb-api-staging.prozorro.gov.ua",
                    "DS_HOST_URL": "https://upload-docs-staging.prozorro.gov.ua",
                    "EDR_HOST_URL": "https://lb-edr-staging.prozorro.gov.ua",
                    "DASU_API_HOST_URL": "https://audit-api-staging.prozorro.gov.ua",
                    "API_VERSION": "2.4",
                    "EDR_VERSION": "0",
                    "PAYMENT_API": "https://integration-staging.prozorro.gov.ua/liqpay",
                    "PAYMENT_API_VERSION": "v1",
                    "AUCTION_REGEXP": "^https?:\\/\\/auction(?:-staging)?\\.prozorro\\.gov\\.ua\\/(esco-)?tenders\\/([0-9A-Fa-f]{32})",
                    "DS_REGEXP": "^https?:\\/\\/public-docs(?:-staging)?\\.prozorro\\.gov\\.ua\\/get\\/([0-9A-Fa-f]{32})",
                ],
                cron: "H 4 * * *",
                branch: "master",
                concurrentBuild: false,
                edr: true,
                dfs: false
        ],
        [
                environment: 'sandbox_2_prozorro',
                params: [
                    "API_HOST_URL": "https://lb-api-sandbox-2.prozorro.gov.ua",
                    "DS_HOST_URL": "https://upload-docs-sandbox-2.prozorro.gov.ua",
                    "EDR_HOST_URL": "https://lb-edr-sandbox-2.prozorro.gov.ua",
                    "DASU_API_HOST_URL": "https://audit-api-sandbox-2.prozorro.gov.ua",
                    "API_VERSION": "2.4",
                    "EDR_VERSION": "0",
                    "PAYMENT_API": "https://integration-sandbox-2.prozorro.gov.ua/liqpay",
                    "PAYMENT_API_VERSION": "v1",
                    "AUCTION_REGEXP": "^https?:\\/\\/auction(?:-sandbox-2)?\\.prozorro\\.gov\\.ua\\/(esco-)?tenders\\/([0-9A-Fa-f]{32})",
                    "DS_REGEXP": "^https?:\\/\\/public-docs(?:-sandbox-2)?\\.prozorro\\.gov\\.ua\\/get\\/([0-9A-Fa-f]{32})",
                ],
                cron: "H 2 * * *",
                branch: "dev_prozorro_2",
                concurrentBuild: false,
                edr: false,
                dfs: false
        ],

].each { Map config ->
    String params = config.params.collect { k,v -> " -v $k:\${$k}" }.join('')

    job("${config.environment}_aboveThresholdEU") {
        parameters defaultParameters(config)
        description("Сценарій: Надпорогова закупівля з публікацією англійською мовою.")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure
        environmentVariables defaultEnv()
        
        String defaultArgs = "-A robot_tests_arguments/openeu.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION \$DFS_QUALIFICATION $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell("$robotWrapper $contractmanagement $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_aboveThresholdEU_no_auction") {
        parameters defaultParameters(config)
        description("Сценарій: Надпорогова закупівля з публікацією англійською мовою.(пропуск аукціона)")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/openeu.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION -v submissionMethodDetails:\"quick(mode:no-auction)\" $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION \$DFS_QUALIFICATION $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell("$robotWrapper $contractmanagement $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_aboveThresholdEU_cancellation") {
        parameters defaultParameters(config)
        description("Сценарій: Надпорогова закупівля з публікацією англійською мовою. Скасування закупівлі.")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $cancellation $defaultArgs -v MODE:openeu$params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_aboveThresholdEU_vat_true_false") {
        parameters defaultParameters(config)
        description("Сценарій: Відкрита процедура (короткий сценарій) оголошена з ПДВ, контракт укладено без ПДВ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/openeu_simple.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION -v submissionMethodDetails:\"quick(mode:no-auction)\" $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION \$DFS_QUALIFICATION $params")
            shell("$robotWrapper $contractsign $defaultArgs -i modify_contract_vat_to_false -i modify_contract_invalid_amount -i modify_contract_invalid_amountNet -i modify_contract_amount_and_amountNet $params")
            shell("$robotWrapper $contractmanagement $defaultArgs -i change_contract_amount_and_amountNet -i modify_contract_view_new_amount_amountNet -i change_amount_and_amountNet_paid $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_aboveThresholdEU_vat_false_false") {
        parameters defaultParameters(config)
        description("Сценарій: Відкрита процедура (короткий сценарій) оголошена без ПДВ, контракт укладено без ПДВ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/openeu_simple.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION -v VAT_INCLUDED:False -v submissionMethodDetails:\"quick(mode:no-auction)\" $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION \$DFS_QUALIFICATION $params")
            shell("$robotWrapper $contractsign $defaultArgs -i modify_contract_invalid_amount -i modify_contract_invalid_amountNet -i modify_contract_amount_and_amountNet -i contract_view_new_amount_and_amountNet $params")
            shell("$robotWrapper $contractmanagement $defaultArgs -i change_contract_amount_and_amountNet -i modify_contract_view_new_amount_amountNet -i change_amount_and_amountNet_paid $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_aboveThresholdEU_vat_false_true") {
        parameters defaultParameters(config)
        description("Сценарій: Відкрита процедура (короткий сценарій) оголошена без ПДВ, контракт укладено з ПДВ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/openeu_simple.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION -v VAT_INCLUDED:False -v submissionMethodDetails:\"quick(mode:no-auction)\" $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION \$DFS_QUALIFICATION $params")
            shell("$robotWrapper $contractsign $defaultArgs -i modify_contract_vat_to_true -i modify_contract_amount_net -i modify_contract_value -i modify_contract_invalid_amountNet -i modify_contract_invalid_amount_tender_vat_false $params")
            shell("$robotWrapper $contractmanagement $defaultArgs -i change_contract_amountNet -i change_contract_amount -i change_amount_paid $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_aboveThresholdUA") {
        parameters defaultParameters(config)
        description("Сценарій: Надпорогова закупівля.")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/openua.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $openProcedure $defaultArgs $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION \$DFS_QUALIFICATION $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell("$robotWrapper $contractmanagement $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_frameworkagreement") {
        parameters defaultParameters(config)
        description("Сценарій: Рамкова угода.")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 18000)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/framework_agreement.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION -v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"default\":{\"tender\":[0,31]}}}}' -v accelerator:2880 $params")
            shell("$robotWrapper $auction_short $defaultArgs -i auction $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION \$DFS_QUALIFICATION $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell("$robotWrapper $agreement $defaultArgs $params")
            shell("$robotWrapper $selection -A robot_tests_arguments/framework_selection_full.txt $params")
            shell("$robotWrapper -o auction_short_framework_output.xml -s auction -A robot_tests_arguments/framework_selection_full.txt $params")
            shell("$robotWrapper -o qualification_framework_output.xml -s qualification -A robot_tests_arguments/framework_selection_full.txt $params")
            shell("$robotWrapper -o contract_framework_output.xml -s contract_signing -A robot_tests_arguments/framework_selection_full.txt $params")
            shell("$robotWrapper -o contract_management_framework_output.xml -s contract_management -A robot_tests_arguments/framework_selection_full.txt $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_frameworkagreement_cancellation") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування закупівлі Рамкова угода")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $cancellation $defaultArgs -e tender_cancellation_stand_still -v MODE:open_framework -v NUMBER_OF_LOTS:1 $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_aboveThresholdUA_cancellation") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування закупівлі (openUA)")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $cancellation $defaultArgs -A robot_tests_arguments/cancellation.txt -v MODE:openua $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_aboveThresholdUA_vat_true_false") {
        parameters defaultParameters(config)
        description("Сценарій: Відкрита процедура (короткий сценарій) оголошена з ПДВ, контракт укладено без ПДВ ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/openua_simple.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $openProcedure $defaultArgs -v submissionMethodDetails:\"quick(mode:no-auction)\" $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION \$DFS_QUALIFICATION $params")
            shell("$robotWrapper $contractsign $defaultArgs -i modify_contract_vat_to_false -i modify_contract_invalid_amount -i modify_contract_invalid_amountNet -i modify_contract_amount_and_amountNet $params")
            shell("$robotWrapper $contractmanagement $defaultArgs -i change_contract_amount_and_amountNet -i modify_contract_view_new_amount_amountNet -i change_amount_and_amountNet_paid $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_aboveThresholdUA_vat_false_false") {
        parameters defaultParameters(config)
        description("Сценарій: Відкрита процедура (короткий сценарій) оголошена без ПДВ, контракт укладено без ПДВ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/openua_simple.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $openProcedure $defaultArgs -v submissionMethodDetails:\"quick(mode:no-auction)\" -v VAT_INCLUDED:False $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION \$DFS_QUALIFICATION $params")
            shell("$robotWrapper $contractsign $defaultArgs --i modify_contract_invalid_amount -i modify_contract_invalid_amountNet -i modify_contract_amount_and_amountNet -i contract_view_new_amount_and_amountNet $params")
            shell("$robotWrapper $contractmanagement $defaultArgs -i change_contract_amount_and_amountNet -i modify_contract_view_new_amount_amountNet -i change_amount_and_amountNet_paid $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_aboveThresholdUA_vat_false_true") {
        parameters defaultParameters(config)
        description("Сценарій: Відкрита процедура (короткий сценарій) оголошена без ПДВ, контракт укладено з ПДВ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/openua_simple.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $openProcedure $defaultArgs -v VAT_INCLUDED:False -v submissionMethodDetails:\"quick(mode:no-auction)\" $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION \$DFS_QUALIFICATION $params")
            shell("$robotWrapper $contractsign $defaultArgs -i modify_contract_vat_to_true -i modify_contract_amount_net -i modify_contract_value -i modify_contract_invalid_amountNet -i modify_contract_invalid_amount_tender_vat_false $params")
            shell("$robotWrapper $contractmanagement $defaultArgs -i change_contract_amountNet -i change_contract_amount -i change_amount_paid $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_below_funders_full") {
        parameters defaultParameters(config)
        description("Сценарій: Допорогова закупівля.")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/below_funders_full.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan $params")
            shell("$robotWrapper $openProcedure $defaultArgs $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell("$robotWrapper $contractmanagement $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_belowThreshold") {
        parameters defaultParameters(config)
        description("Сценарій: Допорогова закупівля.")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/below.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan $params")
            shell("$robotWrapper $openProcedure $defaultArgs $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell("$robotWrapper $contractmanagement $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_belowThreshold_cancellation") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування закупівлі (belowThreshold/ Допорогова закупівля)")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan $params")
            shell("$robotWrapper $cancellation $defaultArgs -e tender_cancellation_stand_still -v MODE:belowThreshold $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_belowThreshold_moz_1") {
        parameters defaultParameters(config)
        description("Сценарій: Допорогова закупівля фармацевтичної продукції")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/mnn_1.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MOZ_INTEGRATION:True $params")
            shell("$robotWrapper $openProcedure $defaultArgs $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell("$robotWrapper $contractmanagement $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_belowThreshold_moz_2") {
        parameters defaultParameters(config)
        description("Сценарій: Допорогова закупівля лікарських засобів")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/mnn_2.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MOZ_INTEGRATION:True $params")
            shell("$robotWrapper $openProcedure $defaultArgs $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell("$robotWrapper $contractmanagement $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_belowThreshold_moz_3") {
        parameters defaultParameters(config)
        description("Сценарій: Допорогова закупівля лікарських засобів без додаткового класифікатора")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/mnn_3.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MOZ_INTEGRATION:True $params")
            shell("$robotWrapper $openProcedure $defaultArgs $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell("$robotWrapper $contractmanagement $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_belowThreshold_moz_validation") {
        parameters defaultParameters(config)
        description("Сценарій: Допорогова закупівля - валідації МНН")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure
        environmentVariables defaultEnv()


        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MOZ_INTEGRATION:True $params")
            shell("$robotWrapper $openProcedure -i create_tender_invalid_2_INN -v MODE:belowThreshold $params")
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MOZ_INTEGRATION:True $params")
            shell("$robotWrapper $openProcedure -i create_tender_invalid_no_add_class -v MODE:belowThreshold $params")
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MOZ_INTEGRATION:True $params")
            shell("$robotWrapper $openProcedure -i create_tender_invalid_no_INN -v MODE:belowThreshold $params")
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MOZ_INTEGRATION:True $params")
            shell("$robotWrapper $openProcedure -i create_tender_invalid_no_atc -v MODE:belowThreshold $params")
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MOZ_INTEGRATION:True $params")
            shell("$robotWrapper $openProcedure -i create_tender_invalid_no_atc_2 -v MODE:belowThreshold $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_belowThreshold_vat_true_false") {
        parameters defaultParameters(config)
        description("Допорогова закупівля (мінімальний набір тест кейсів) оголошена з ПДВ, контракт укладено без ПДВ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/below_simple.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan $params")
            shell("$robotWrapper $openProcedure $defaultArgs -v submissionMethodDetails:\"quick(mode:no-auction)\" $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs -i modify_contract_vat_to_false -i modify_contract_invalid_amount -i modify_contract_invalid_amountNet -i modify_contract_amount_and_amountNet $params")
            shell("$robotWrapper $contractmanagement $defaultArgs -i change_contract_amount_and_amountNet -i modify_contract_view_new_amount_amountNet -i change_amount_and_amountNet_paid $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_belowThreshold_vat_false_false") {
        parameters defaultParameters(config)
        description("Допорогова закупівля (мінімальний набір тест кейсів) оголошена без ПДВ, контракт укладено без ПДВ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/below_simple.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan $params")
            shell("$robotWrapper $openProcedure $defaultArgs -v submissionMethodDetails:\"quick(mode:no-auction)\" -v VAT_INCLUDED:False $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs -i modify_contract_invalid_amount -i modify_contract_invalid_amountNet -i modify_contract_amount_and_amountNet -i contract_view_new_amount_and_amountNet $params")
            shell("$robotWrapper $contractmanagement $defaultArgs -i change_contract_amount_and_amountNet -i modify_contract_view_new_amount_amountNet -i change_amount_and_amountNet_paid $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_belowThreshold_vat_false_true") {
        parameters defaultParameters(config)
        description("Допорогова закупівля (мінімальний набір тест кейсів) оголошено без ПДВ, контракт укладено з ПДВ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/below_simple.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan $params")
            shell("$robotWrapper $openProcedure $defaultArgs -v submissionMethodDetails:\"quick(mode:no-auction)\" -v VAT_INCLUDED:False $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs -i modify_contract_vat_to_true -i modify_contract_amount_net -i modify_contract_value -i modify_contract_invalid_amountNet -i modify_contract_invalid_amount_tender_vat_false $params")
            shell("$robotWrapper $contractmanagement $defaultArgs -i change_contract_amountNet -i change_contract_amount -i change_amount_paid $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_competitiveDialogueEU") {
        parameters defaultParameters(config)
        description("Сценарій: Конкурентний діалог з публікацією англійською мовою")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/competitive_dialogue.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION \$DFS_QUALIFICATION $params")
            shell("$robotWrapper $contractsign $defaultArgs -i modify_contract_invalid_amount -i modify_contract_invalid_amountNet_tender_vat_true -i modify_contract_amount_net -i modify_contract_value $params")
            shell("$robotWrapper $contractmanagement $defaultArgs -i change_contract_amountNet -i change_contract_amount -i change_amount_paid $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_competitiveDialogueEU_stage1") {
        parameters defaultParameters(config)
        description("Сценарій: Конкурентний діалог з публікацією англійською мовою")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/competitive_dialogue.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_competitiveDialogueEU_vat_true_false") {
        parameters defaultParameters(config)
        description("Сценарій: Конкурентний діалог з публікацією англійською мовою")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/competitive_dialogue_simple_vat_testing.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION -v submissionMethodDetails:\"quick(mode:no-auction)\" $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION \$DFS_QUALIFICATION $params")
            shell("$robotWrapper $contractsign $defaultArgs -i modify_contract_vat_to_false -i modify_contract_invalid_amount -i modify_contract_invalid_amountNet -i modify_contract_amount_and_amountNet $params")
            shell("$robotWrapper $contractmanagement $defaultArgs -i change_contract_amount_and_amountNet -i modify_contract_view_new_amount_amountNet -i change_amount_and_amountNet_paid $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_competitiveDialogueEU_vat_false_false") {
        parameters defaultParameters(config)
        description("Сценарій: Конкурентний діалог з публікацією англійською мовою")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/competitive_dialogue_simple_vat_testing.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION -v submissionMethodDetails:\"quick(mode:no-auction)\" -v VAT_INCLUDED:False $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION \$DFS_QUALIFICATION $params")
            shell("$robotWrapper $contractsign $defaultArgs -i modify_contract_invalid_amount -i modify_contract_invalid_amountNet -i modify_contract_amount_and_amountNet -i contract_view_new_amount_and_amountNet $params")
            shell("$robotWrapper $contractmanagement $defaultArgs -i change_contract_amount_and_amountNet -i modify_contract_view_new_amount_amountNet -i change_amount_and_amountNet_paid $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_competitiveDialogueEU_vat_false_true") {
        parameters defaultParameters(config)
        description("Сценарій: Конкурентний діалог з публікацією англійською мовою")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/competitive_dialogue_simple_vat_testing.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION -v submissionMethodDetails:\"quick(mode:no-auction)\" -v VAT_INCLUDED:False $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION  \$DFS_QUALIFICATION $params")
            shell("$robotWrapper $contractsign $defaultArgs -i modify_contract_vat_to_true -i modify_contract_amount_net -i modify_contract_value -i modify_contract_invalid_amountNet -i modify_contract_invalid_amount_tender_vat_false $params")
            shell("$robotWrapper $contractmanagement $defaultArgs -i change_contract_amountNet -i change_contract_amount -i change_amount_paid $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_competitiveDialogueEU_cancellation") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування закупівлі (competitive dialogue)")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $cancellation $defaultArgs -v MODE:open_competitive_dialogue $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_competitiveDialogueUA") {
        parameters defaultParameters(config)
        description("Сценарій: Конкурентний діалог для надпорогових закупівель українською мовою")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/competitive_dialogue_UA.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION \$DFS_QUALIFICATION $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell("$robotWrapper $contractmanagement $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_competitiveDialogueUA_cancellation") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування закупівлі (competitive dialogue)")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $cancellation $defaultArgs -v MODE:open_competitive_dialogue -v DIALOGUE_TYPE:UA $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_competitiveDialogueUA_vat_true_false") {
        parameters defaultParameters(config)
        description("Сценарій: Конкурентний діалог для надпорогових закупівель українською мовою")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/competitive_dialogue_simple_UA_vat_testing.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION -v submissionMethodDetails:\"quick(mode:no-auction)\" $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION \$DFS_QUALIFICATION $params")
            shell("$robotWrapper $contractsign $defaultArgs -i modify_contract_vat_to_false -i modify_contract_invalid_amount -i modify_contract_invalid_amountNet -i modify_contract_amount_and_amountNet $params")
            shell("$robotWrapper $contractmanagement $defaultArgs -i change_contract_amount_and_amountNet -i modify_contract_view_new_amount_amountNet -i change_amount_and_amountNet_paid $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_competitiveDialogueUA_vat_false_false") {
        parameters defaultParameters(config)
        description("Сценарій: Конкурентний діалог для надпорогових закупівель українською мовою")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/competitive_dialogue_simple_UA_vat_testing.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION -v submissionMethodDetails:\"quick(mode:no-auction)\" -v VAT_INCLUDED:False $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION \$DFS_QUALIFICATION $params")
            shell("$robotWrapper $contractsign $defaultArgs -i modify_contract_invalid_amount -i modify_contract_invalid_amountNet -i modify_contract_amount_and_amountNet -i contract_view_new_amount_and_amountNet $params")
            shell("$robotWrapper $contractmanagement $defaultArgs -i change_contract_amount_and_amountNet -i modify_contract_view_new_amount_amountNet -i change_amount_and_amountNet_paid $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_competitiveDialogueUA_vat_false_true") {
        parameters defaultParameters(config)
        description("Сценарій: Конкурентний діалог для надпорогових закупівель українською мовою")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/competitive_dialogue_simple_UA_vat_testing.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION -v submissionMethodDetails:\"quick(mode:no-auction)\" -v VAT_INCLUDED:False $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION \$DFS_QUALIFICATION $params")
            shell("$robotWrapper $contractsign $defaultArgs -i modify_contract_vat_to_true -i modify_contract_amount_net -i modify_contract_value -i modify_contract_invalid_amountNet -i modify_contract_invalid_amount_tender_vat_false $params")
            shell("$robotWrapper $contractmanagement $defaultArgs -i change_contract_amountNet -i change_contract_amount -i change_amount_paid $params")
            shell(shellRebot)
        }
    }

    ["cancelled", "closed", "completed", "stopped"].each { String scenario ->
        job("${config.environment}_dasu_$scenario") {
            parameters defaultParameters(config)
            description("Сценарій: ДАСУ")
            keepDependencies(false)
            disabled(false)
            concurrentBuild(config.concurrentBuild)
            scm defaultScm
            publishers defaultPublishers
            wrappers defaultWrappers(true)
            configure defaultConfigure
            environmentVariables defaultEnv()

            steps {
                shell(shellBuildout)
                shell(shellPhantom)
                shell("$robotWrapper $planning -i create_plan -i find_plan $params")
                shell("$robotWrapper -o monitoring_output.xml -s monitoring -A robot_tests_arguments/dasu_${scenario}.txt $params")
                shell(shellRebot)
            }
        }
    }

    ["negotiation", "negotiation.quick", "reporting"].each { String scenario ->
        job("${config.environment}_sb_$scenario") {
            parameters defaultParameters(config)
            description("Сценарій: Переговорна процедура")
            keepDependencies(false)
            disabled(false)
            concurrentBuild(config.concurrentBuild)
            scm defaultScm
            publishers defaultPublishers
            wrappers defaultWrappers(scenario != 'negotiation.quick', 10800)
            configure defaultConfigure
            environmentVariables defaultEnv()

            steps {
                shell(shellBuildout)
                shell(shellPhantom)
                shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:$scenario $params")
                shell("$robotWrapper -s $scenario $params")
                shell(shellRebot)
            }
        }
    }

    job("${config.environment}_reporting_cancellation") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування закупівлі reporting")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/limited_cancellation.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:reporting $params")
            shell("$robotWrapper $cancellation $defaultArgs -e tender_cancellation_stand_still -v MODE:reporting $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_negotiation_cancellation") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування закупівлі negotiation")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/limited_cancellation.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:negotiation $params")
            shell("$robotWrapper $cancellation $defaultArgs -v MODE:negotiation $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_negotiation.quick_cancellation") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування закупівлі negotiation.quick")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/limited_cancellation.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:negotiation.quick $params")
            shell("$robotWrapper $cancellation $defaultArgs -v MODE:negotiation.quick $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_planning_belowThreshold") {
        parameters defaultParameters(config)
        description("Сценарій: Планування допорогова процедура")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper -o planning_output.xml -s planning -A robot_tests_arguments/planning.txt -v NUMBER_OF_ITEMS:2 -v TENDER_MEAT:False -v ITEM_MEAT:False $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_planning_framework_agreement") {
        parameters defaultParameters(config)
        description("Сценарій: Планування рамкової угоди")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper -o planning_output.xml -s planning -A robot_tests_arguments/planning.txt -i closeframework_period -v MODE:closeFrameworkAgreementUA -v NUMBER_OF_ITEMS:2 -v TENDER_MEAT:False -v ITEM_MEAT:False $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_feed_reading") {
        parameters defaultParameters(config)
        description("Сценарій: Вичітування довільного набіру даних с ЦБД")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper -o feed_plans_output.xml -s feed_plans -v FEED_ITEMS_NUMBER:150 $params")
            shell("$robotWrapper -o feed_tenders_output.xml -s feed_tenders -v FEED_ITEMS_NUMBER:150 $params")
            shell(shellRebot)
        }
    }


    job("${config.environment}_single_item_tender") {
        parameters defaultParameters(config)
        description("Сценарій: Допорогова закупівля з однією номенклатурою до аукціону")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/single_item_tender.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan $params")
            shell("$robotWrapper $openProcedure $defaultArgs $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell("$robotWrapper $contractmanagement $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_aboveThresholdUA_defence_one_bid") {
        parameters defaultParameters(config)
        description("Сценарій: Надпорогова закупівля. Переговорна процедура для потреб оборони (з одним учасником).")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/openUAdefense_one_bid.txt"

         steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA.defense $params")
            shell("$robotWrapper $openProcedure $defaultArgs -i answer_question_to_tender $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION \$DFS_QUALIFICATION $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell(shellRebot)
         }
    }

    job("${config.environment}_esco") {
        parameters defaultParameters(config)
        description("Сценарій: Відкриті торги для закупівлі енергосервісу")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/esco_testing.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION $params")
            shell("$robotWrapper $auction_short $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION \$DFS_QUALIFICATION $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_esco_cancellation") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування закупівлі Відкриті торги для закупівлі енергосервісу")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $cancellation $defaultArgs -v MODE:open_esco -v FUNDING_KIND:budget $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_planning_aboveThresholdUA") {
        parameters defaultParameters(config)
        description("Сценарій: Планування Надпорогова закупівля")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper -o planning_output.xml -s planning -A robot_tests_arguments/planning.txt -v MODE:aboveThresholdUA $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_planning_aboveThresholdEU") {
        parameters defaultParameters(config)
        description("Сценарій: Планування Надпорогова закупівля з публікацією англійською мовою")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper -o planning_output.xml -s planning -A robot_tests_arguments/planning.txt -v MODE:aboveThresholdEU $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_planning_aboveThresholdUA.defense") {
        parameters defaultParameters(config)
        description("Сценарій: Планування Надпорогова закупівля. Переговорна процедура для потреб оборони")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper -o planning_output.xml -s planning -A robot_tests_arguments/planning.txt -v MODE:aboveThresholdUA.defense $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_planning_esco") {
        parameters defaultParameters(config)
        description("Сценарій: Планування Відкриті торги для закупівлі енергосервісу")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper -o planning_output.xml -s planning -A robot_tests_arguments/planning.txt -v MODE:esco $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_planning_reporting") {
        parameters defaultParameters(config)
        description("Сценарій: Планування Переговорна процедура")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper -o planning_output.xml -s planning -A robot_tests_arguments/planning.txt -v MODE:reporting $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_planning_negotiation") {
        parameters defaultParameters(config)
        description("Сценарій: Планування Переговорна процедура")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper -o planning_output.xml -s planning -A robot_tests_arguments/planning.txt -v MODE:negotiation $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_planning_negotiation.quick") {
        parameters defaultParameters(config)
        description("Сценарій: Планування Переговорна процедура. Скорочена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper -o planning_output.xml -s planning -A robot_tests_arguments/planning.txt -v MODE:negotiation.quick $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_plan_tender_validations") {
        parameters defaultParameters(config)
        description("Сценарій: Валідації План-Тендер")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan_two_buyers -i create_plan_no_buyers $params")
            shell("$robotWrapper $planning -i create_plan -i find_plan $params")
            shell("$robotWrapper $openProcedure -i create_tender_invalid_edrpou -v MODE:belowThreshold $params")
            shell("$robotWrapper $openProcedure -i create_tender_invalid_schema -v MODE:belowThreshold $params")
            shell("$robotWrapper $openProcedure -i create_tender_invalid_cpv -v MODE:belowThreshold $params")
            shell("$robotWrapper $openProcedure -i create_tender_invalid_procurementMethodType -v MODE:belowThreshold $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_below_cost") {
        parameters defaultParameters(config)
        description("Сценарій: Допорогова закупівля з індексом UA-ROAD")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/below.txt -e auction -e add_bid_doc_after_tendering_period_by_provider -e modify_bid_doc_after_tendering_period_by_provider"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v ROAD_INDEX:True $params")
            shell("$robotWrapper $openProcedure $defaultArgs -v submissionMethodDetails:\"quick(mode:no-auction)\" $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell("$robotWrapper $contractmanagement $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_below_gmdn") {
        parameters defaultParameters(config)
        description("Сценарій: Допорогова закупівля з індексом GMDN")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/below.txt -e auction -e add_bid_doc_after_tendering_period_by_provider -e modify_bid_doc_after_tendering_period_by_provider"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v GMDN_INDEX:True $params")
            shell("$robotWrapper $openProcedure $defaultArgs -v submissionMethodDetails:\"quick(mode:no-auction)\" $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell("$robotWrapper $contractmanagement $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_cost_gmdn_validations") {
        parameters defaultParameters(config)
        description("Сценарій: Валідації cost/gmdn")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure
        environmentVariables defaultEnv()

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v ROAD_INDEX:True $params")
            shell("$robotWrapper $openProcedure -i create_tender_cost_no_addclass -v MODE:belowThreshold $params")

            shell("$robotWrapper $planning -i create_plan -i find_plan -v ROAD_INDEX:True $params")
            shell("$robotWrapper $openProcedure -i create_tender_cost_2_addclass -v MODE:belowThreshold $params")

            shell("$robotWrapper $planning -i create_plan -i find_plan -v ROAD_INDEX:True $params")
            shell("$robotWrapper $openProcedure -i create_tender_cost_invalid_addclass_id -v MODE:belowThreshold $params")

            shell("$robotWrapper $planning -i create_plan -i find_plan -v ROAD_INDEX:True $params")
            shell("$robotWrapper $openProcedure -i create_tender_cost_invalid_addclass_description -v MODE:belowThreshold $params")

            shell("$robotWrapper $planning -i create_plan -i find_plan -v GMDN_INDEX:True $params")
            shell("$robotWrapper $openProcedure -i create_tender_cost_invalid_addclass -v MODE:belowThreshold $params")

            shell("$robotWrapper $planning -i create_plan -i find_plan -v GMDN_INDEX:True $params")
            shell("$robotWrapper $openProcedure -i create_tender_gmdn_no_addclass -v MODE:belowThreshold $params")

            shell("$robotWrapper $planning -i create_plan -i find_plan -v GMDN_INDEX:True $params")
            shell("$robotWrapper $openProcedure -i create_tender_gmdn_2_addclass -v MODE:belowThreshold $params")

            shell("$robotWrapper $planning -i create_plan -i find_plan -v GMDN_INDEX:True $params")
            shell("$robotWrapper $openProcedure -i create_tender_gmdn_invalid_addclass_id -v MODE:belowThreshold $params")

            shell("$robotWrapper $planning -i create_plan -i find_plan -v GMDN_INDEX:True $params")
            shell("$robotWrapper $openProcedure -i create_tender_gmdn_inn_addclass -v MODE:belowThreshold $params")

            shell("$robotWrapper $planning -i create_plan -i find_plan -v ROAD_INDEX:True $params")
            shell("$robotWrapper $openProcedure -i create_tender_gmdn_invalid_addclass -v MODE:belowThreshold $params")

            shell(shellRebot)
        }
    }

    job("${config.environment}_aboveThresholdEU_DFS") {
        parameters defaultParameters(config)
        description("Сценарій: Надпорогова закупівля з публікацією англійською мовою.")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/openeu_dfs.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION -v submissionMethodDetails:\"quick(mode:no-auction)\" $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION \$DFS_QUALIFICATION $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell("$robotWrapper $contractmanagement $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_aboveThresholdUA_DFS") {
        parameters defaultParameters(config)
        description("Сценарій: Надпорогова закупівля.")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/openua_dfs.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $openProcedure $defaultArgs -v submissionMethodDetails:\"quick(mode:no-auction)\" $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION \$DFS_QUALIFICATION $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell("$robotWrapper $contractmanagement $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_competitiveDialogueEU_DFS") {
        parameters defaultParameters(config)
        description("Сценарій: Конкурентний діалог з публікацією англійською мовою")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/competitive_dialogue.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION -v submissionMethodDetails:\"quick(mode:no-auction)\" $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION \$DFS_QUALIFICATION $params")
            shell("$robotWrapper $contractsign $defaultArgs -i modify_contract_invalid_amount -i modify_contract_invalid_amountNet_tender_vat_true -i modify_contract_amount_net -i modify_contract_value $params")
            shell("$robotWrapper $contractmanagement $defaultArgs -i change_contract_amountNet -i change_contract_amount -i change_amount_paid $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_competitiveDialogueUA_DFS") {
        parameters defaultParameters(config)
        description("Сценарій: Конкурентний діалог для надпорогових закупівель українською мовою")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/competitive_dialogue_UA.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION -v submissionMethodDetails:\"quick(mode:no-auction)\" $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION \$DFS_QUALIFICATION $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell("$robotWrapper $contractmanagement $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_esco_DFS") {
        parameters defaultParameters(config)
        description("Сценарій: Відкриті торги для закупівлі енергосервісу")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/esco_testing.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION -v submissionMethodDetails:\"quick(mode:no-auction)\" $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION \$DFS_QUALIFICATION $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_openeu_complaint_tender_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, прийнята та задоволена АМКУ та Учасником виконано рішення АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_resolved.txt"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs -v 'BROKERS_PARAMS:{\"Default\":{\"intervals\":{\"openeu\":{\"tender\":[1,5]}}}}' -v accelerator:14400 $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_openeu_complaint_tender_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, позначена Учасником як помилково створена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_mistaken.txt"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs -v 'BROKERS_PARAMS:{\"Default\":{\"intervals\":{\"openeu\":{\"tender\":[1,5]}}}}' -v accelerator:14400 $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_openeu_complaint_tender_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, прийнята до розгляду та відхилена АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_declined.txt"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs -v 'BROKERS_PARAMS:{\"Default\":{\"intervals\":{\"openeu\":{\"tender\":[1,5]}}}}' -v accelerator:14400 $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_openeu_complaint_tender_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, прийнята, розгляду зупинено АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_stopped.txt"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints  $defaultArgs -v 'BROKERS_PARAMS:{\"Default\":{\"intervals\":{\"openeu\":{\"tender\":[1,5]}}}}' -v accelerator:14400 $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_openeu_complaint_tender_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, АМКУ залишив скаргу без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_invalid.txt"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints  $defaultArgs -v 'BROKERS_PARAMS:{\"Default\":{\"intervals\":{\"openeu\":{\"tender\":[1,5]}}}}' -v accelerator:14400 $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_openua_complaint_award_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на визначення переможця, прийнята та задоволена АМКУ та Учасником виконано рішення АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_award_resolved.txt"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $complaints  $defaultArgs $no_auction $accelerate_openeu $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_openua_complaint_award_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на визначення переможця, позначена Учасником як помилково створена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

         String defaultArgs = "-A robot_tests_arguments/complaint_award_mistaken.txt"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $complaints  $defaultArgs $no_auction -v 'BROKERS_PARAMS:{\"Default\":{\"intervals\":{\"openua\":{\"tender\":[1,5]}}}}' -v accelerator:14400 $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_openua_complaint_award_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на визначення переможця, прийнята до розгляду та відхилена АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_award_declined.txt"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $complaints  $defaultArgs $no_auction -v 'BROKERS_PARAMS:{\"Default\":{\"intervals\":{\"openua\":{\"tender\":[1,5]}}}}' -v accelerator:14400 $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_openua_complaint_award_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на визначення переможця, прийнята, розгляду зупинено АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_award_stopped.txt"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $complaints  $defaultArgs $no_auction -v 'BROKERS_PARAMS:{\"Default\":{\"intervals\":{\"openua\":{\"tender\":[1,5]}}}}' -v accelerator:14400 $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_openua_complaint_award_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на визначення переможця, АМКУ залишив скаргу без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_award_invalid.txt"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $complaints $defaultArgs $no_auction -v 'BROKERS_PARAMS:{\"Default\":{\"intervals\":{\"openua\":{\"tender\":[1,5]}}}}' -v accelerator:14400 $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_openeu_complaint_qualification_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на пре-кваліфікацію учасника задоволена Замовником")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_qualification_resolved.txt"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs -v 'BROKERS_PARAMS:{\"Default\":{\"intervals\":{\"openeu\":{\"tender\":[1,5]}}}}' -v accelerator:14400 $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_openeu_complaint_qualification_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на пре-кваліфікацію учасника створена помилково")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_qualification_mistaken.txt"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs -v 'BROKERS_PARAMS:{\"Default\":{\"intervals\":{\"openeu\":{\"tender\":[1,5]}}}}' -v accelerator:14400 $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_openeu_complaint_qualification_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на пре-кваліфікацію учасника залишена без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_qualification_invalid.txt"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs -v 'BROKERS_PARAMS:{\"Default\":{\"intervals\":{\"openeu\":{\"tender\":[1,5]}}}}' -v accelerator:14400 $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_openeu_complaint_qualification_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на пре-кваліфікацію учасника відхилена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_qualification_declined.txt"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs -v 'BROKERS_PARAMS:{\"Default\":{\"intervals\":{\"openeu\":{\"tender\":[1,5]}}}}' -v accelerator:14400 $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_openeu_complaint_qualification_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на пре-кваліфікацію учасника розгляд зупинено")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_qualification_stopped.txt"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs -v 'BROKERS_PARAMS:{\"Default\":{\"intervals\":{\"openeu\":{\"tender\":[1,5]}}}}' -v accelerator:14400 $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_openeu_complaint_lot_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота виконана Замовником")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_resolved.txt"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs -v 'BROKERS_PARAMS:{\"Default\":{\"intervals\":{\"openeu\":{\"tender\":[1,5]}}}}' -v accelerator:14400 $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_openeu_complaint_lot_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота створена помилково")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_mistaken.txt"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs -v 'BROKERS_PARAMS:{\"Default\":{\"intervals\":{\"openeu\":{\"tender\":[1,5]}}}}' -v accelerator:14400 $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_openeu_complaint_lot_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота залишена без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_invalid.txt"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs -v 'BROKERS_PARAMS:{\"Default\":{\"intervals\":{\"openeu\":{\"tender\":[1,5]}}}}' -v accelerator:14400 $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_openeu_complaint_lot_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота відхилена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_declined.txt"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs -v 'BROKERS_PARAMS:{\"Default\":{\"intervals\":{\"openeu\":{\"tender\":[1,5]}}}}' -v accelerator:14400 $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_openeu_complaint_lot_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота розгляд зупинено")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_stopped.txt"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs -v 'BROKERS_PARAMS:{\"Default\":{\"intervals\":{\"openeu\":{\"tender\":[1,5]}}}}' -v accelerator:14400 $params")
            shell(shellRebot)
        }
    }



    multiJob(config.environment) {
        authenticationToken(remoteToken)
        parameters defaultParameters(config)
        concurrentBuild(config.concurrentBuild)
        triggers defaultTriggers(config.cron)
        steps {
            phase("Test") {
                def innerJobs = [
                    "${config.environment}_aboveThresholdEU",
                    "${config.environment}_aboveThresholdEU_no_auction",
                    "${config.environment}_aboveThresholdEU_cancellation",
                    "${config.environment}_aboveThresholdEU_vat_true_false",
                    "${config.environment}_aboveThresholdEU_vat_false_false",
                    "${config.environment}_aboveThresholdEU_vat_false_true",
                    "${config.environment}_aboveThresholdUA",
                    "${config.environment}_frameworkagreement",
                    "${config.environment}_frameworkagreement_cancellation",
                    "${config.environment}_aboveThresholdUA_cancellation",
                    "${config.environment}_aboveThresholdUA_vat_true_false",
                    "${config.environment}_aboveThresholdUA_vat_false_false",
                    "${config.environment}_aboveThresholdUA_vat_false_true",
                    "${config.environment}_below_funders_full",
                    "${config.environment}_belowThreshold",
                    "${config.environment}_belowThreshold_VAT_False",
                    "${config.environment}_belowThreshold_cancellation",
                    "${config.environment}_belowThreshold_moz",
                    "${config.environment}_belowThreshold_vat_true_false",
                    "${config.environment}_belowThreshold_vat_false_false",
                    "${config.environment}_belowThreshold_vat_false_true",
                    "${config.environment}_competitiveDialogueEU",
                    "${config.environment}_competitiveDialogueEU_cancellation",
                    "${config.environment}_competitiveDialogueEU_stage1",
                    "${config.environment}_competitiveDialogueEU_vat_true_false",
                    "${config.environment}_competitiveDialogueEU_vat_false_false",
                    "${config.environment}_competitiveDialogueEU_vat_false_true",
                    "${config.environment}_competitiveDialogueUA",
                    "${config.environment}_competitiveDialogueUA_cancellation",
                    "${config.environment}_competitiveDialogueUA_vat_true_false",
                    "${config.environment}_competitiveDialogueUA_vat_false_false",
                    "${config.environment}_competitiveDialogueUA_vat_false_true",
                    "${config.environment}_sb_negotiation",
                    "${config.environment}_sb_negotiation.quick",
                    "${config.environment}_sb_reporting",
                    "${config.environment}_negotiation_cancellation",
                    "${config.environment}_negotiation.quick_cancellation",
                    "${config.environment}_reporting_cancellation",
                    "${config.environment}_feed_reading",
                    "${config.environment}_single_item_tender",
                    "${config.environment}_aboveThresholdUA_defence_one_bid",
                    "${config.environment}_esco",
                    "${config.environment}_esco_cancellation",
                    "${config.environment}_belowThreshold_moz_1",
                    "${config.environment}_belowThreshold_moz_2",
                    "${config.environment}_belowThreshold_moz_3",
                    "${config.environment}_belowThreshold_moz_validation",
                    "${config.environment}_below_cost",
                    "${config.environment}_below_gmdn",
                    "${config.environment}_cost_gmdn_validations",
                    "${config.environment}_planning_aboveThresholdUA",
                    "${config.environment}_planning_aboveThresholdEU",
                    "${config.environment}_planning_aboveThresholdUA.defense",
                    "${config.environment}_planning_esco",
                    "${config.environment}_planning_reporting",
                    "${config.environment}_planning_negotiation",
                    "${config.environment}_planning_negotiation.quick",
                    "${config.environment}_plan_tender_validations",
                    "${config.environment}_planning_belowThreshold",
                    "${config.environment}_planning_framework_agreement",
                    "${config.environment}_dasu_cancelled",
                    "${config.environment}_dasu_closed",
                    "${config.environment}_dasu_completed",
                    "${config.environment}_dasu_stopped",
                    "${config.environment}_openeu_complaint_tender_resolved",
                    "${config.environment}_openeu_complaint_tender_mistaken",
                    "${config.environment}_openeu_complaint_tender_declined",
                    "${config.environment}_openeu_complaint_tender_stopped",
                    "${config.environment}_openeu_complaint_tender_invalid",
                    "${config.environment}_openua_complaint_award_resolved",
                    "${config.environment}_openua_complaint_award_mistaken",
                    "${config.environment}_openua_complaint_award_declined",
                    "${config.environment}_openua_complaint_award_stopped",
                    "${config.environment}_openua_complaint_award_invalid",
                    "${config.environment}_openeu_complaint_qualification_resolved",
                    "${config.environment}_openeu_complaint_qualification_mistaken",
                    "${config.environment}_openeu_complaint_qualification_invalid",
                    "${config.environment}_openeu_complaint_qualification_declined",
                    "${config.environment}_openeu_complaint_qualification_stopped",
                    "${config.environment}_openeu_complaint_lot_resolved",
                    "${config.environment}_openeu_complaint_lot_mistaken",
                    "${config.environment}_openeu_complaint_lot_invalid",
                    "${config.environment}_openeu_complaint_lot_declined",
                    "${config.environment}_openeu_complaint_lot_stopped",

                ]
                if (config.environment == 'staging_prozorro') {
                    innerJobs.addAll([
                            "${config.environment}_aboveThresholdEU_DFS",
                            "${config.environment}_aboveThresholdUA_DFS",
                            "${config.environment}_competitiveDialogueEU_DFS",
                            "${config.environment}_competitiveDialogueUA_DFS",
                            "${config.environment}_esco_DFS"

                    ])
                }
                innerJobs.each { String scenario -> phaseJob(scenario) {
                    currentJobParameters(true)
                    abortAllJobs(false)
                }}
            }         
        }
    }

    listView("${config.environment}") {
        jobs {
            regex("^${config.environment}.*")
        }
        columns {
            status()
            weather()
            name()
            lastSuccess()
            lastFailure()
            lastDuration()
            buildButton()
        }
    }
}

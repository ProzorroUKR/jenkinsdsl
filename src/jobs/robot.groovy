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

def defaultEnv_dfs() {
    return {
    groovy("if (DFS.toBoolean()) {return [DFS_QUALIFICATION: '-i awards_check_by_dfs']}")
    }
}

String shellBuildout = "sleep \$((RANDOM % 600))\nping -c 10 8.8.8.8\nping -c 10 github.com\npython2 bootstrap.py\nbin/buildout -N buildout:always-checkout=force\nbin/develop update -f"
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
String fast_auction = "-v submissionMethodDetails:\"new-auction;quick(mode:fast-auction)\""
String priceQuotation = "-o priceQuotation_output.xml -s priceQuotationProcedure"
String old_auction = "-o old_auction_output.xml -s auction_long"

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
                    "API_VERSION": "2.5",
                    "EDR_VERSION": "0",
                    "AUCTION_REGEXP": "^http?:\\/\\/auction\\.\${RELEASE_NAME}\\.k8s\\.prozorro\\.gov\\.ua\\/(esco-)?tenders\\/([0-9A-Fa-f]{32})",
                    "DS_REGEXP": "^http?:\\/\\/ds\\.k8s\\.prozorro\\.gov\\.ua\\/get\\/([0-9A-Fa-f]{32})",
                ],
                cron: null,
                branch: "dev_prozorro_2",
                concurrentBuild: true,
                edr: false,
                dfs: false
        ],
        [
                environment: 'staging_prozorro',
                params: [
                    "API_HOST_URL": "https://lb-api-staging.prozorro.gov.ua",
                    "DS_HOST_URL": "https://upload-docs-staging.prozorro.gov.ua",
                    "EDR_HOST_URL": "https://lb-edr-staging.prozorro.gov.ua",
                    "DASU_API_HOST_URL": "https://audit-api-staging.prozorro.gov.ua",
                    "DASU_API_VERSION": "2.5",
                    "API_VERSION": "2.5",
                    "EDR_VERSION": "0",
                    "PAYMENT_API": "https://integration-staging.prozorro.gov.ua/liqpay",
                    "PAYMENT_API_VERSION": "v1",
                    "AUCTION_REGEXP": "^https?:\\/\\/auction(?:-new)?(?:-staging)?\\.prozorro\\.gov\\.ua\\/(esco-)?tenders\\/([0-9A-Fa-f]{32})",
                    "DS_REGEXP": "^https?:\\/\\/public-docs(?:-staging)?\\.prozorro\\.gov\\.ua\\/get\\/([0-9A-Fa-f]{32})",
                ],
                cron: "0 4 * * *",
                branch: "broker",
                concurrentBuild: false,
                edr: true,
                dfs: true
        ],
        [
                environment: 'sandbox_2_prozorro',
                params: [
                    "API_HOST_URL": "https://lb-api-sandbox-2.prozorro.gov.ua",
                    "DS_HOST_URL": "https://upload-docs-sandbox-2.prozorro.gov.ua",
                    "EDR_HOST_URL": "https://lb-edr-sandbox-2.prozorro.gov.ua",
                    "DASU_API_HOST_URL": "https://audit-api-sandbox-2.prozorro.gov.ua",
                    "DASU_API_VERSION": "2.5",
                    "API_VERSION": "2.5",
                    "EDR_VERSION": "1.0",
                    "PAYMENT_API": "https://integration-sandbox-2.prozorro.gov.ua/liqpay",
                    "PAYMENT_API_VERSION": "v1",
                    "AUCTION_REGEXP": "^https?:\\/\\/auction(?:-sandbox-2)?\\.prozorro\\.gov\\.ua\\/(esco-)?tenders\\/([0-9A-Fa-f]{32})",
                    "DS_REGEXP": "^https?:\\/\\/public-docs(?:-sandbox-2)?\\.prozorro\\.gov\\.ua\\/get\\/([0-9A-Fa-f]{32})",
                ],
                cron: "0 1 * * *",
                branch: "dev_prozorro_2",
                concurrentBuild: false,
                edr: true,
                dfs: true
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
        environmentVariables {defaultEnv() defaultEnv_dfs()}
        
        String defaultArgs = "-A robot_tests_arguments/openeu.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION $fast_auction $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION \$DFS_QUALIFICATION $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell("$robotWrapper $contractmanagement $defaultArgs $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION $no_auction $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION -v VAT_INCLUDED:False $no_auction $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION -v VAT_INCLUDED:False $no_auction $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs $fast_auction $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs $fast_auction \$EDR_PRE_QUALIFICATION -v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"default\":{\"tender\":[0,31]}}}}' -v accelerator:2880 $params")
            shell("$robotWrapper $auction_short $defaultArgs -i auction $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION \$DFS_QUALIFICATION $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell("$robotWrapper $agreement $defaultArgs $params")
            shell("$robotWrapper $selection -A robot_tests_arguments/framework_selection_full.txt $fast_auction $params")
            shell("$robotWrapper -o auction_short_framework_output.xml -s auction -A robot_tests_arguments/framework_selection_full.txt $params")
            shell("$robotWrapper -o qualification_framework_output.xml -s qualification -A robot_tests_arguments/framework_selection_full.txt $params")
            shell("$robotWrapper -o contract_framework_output.xml -s contract_signing -A robot_tests_arguments/framework_selection_full.txt $params")
            shell("$robotWrapper -o contract_management_framework_output.xml -s contract_management -A robot_tests_arguments/framework_selection_full.txt $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs $no_auction $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs $no_auction -v VAT_INCLUDED:False $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs -v VAT_INCLUDED:False $no_auction $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs $fast_auction $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs $fast_auction $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell("$robotWrapper $contractmanagement $defaultArgs $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs $fast_auction $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs $fast_auction $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs $fast_auction $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs $no_auction $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs $no_auction -v VAT_INCLUDED:False $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs $no_auction -v VAT_INCLUDED:False $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs $fast_auction \$EDR_PRE_QUALIFICATION $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION $no_auction $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION $no_auction -v VAT_INCLUDED:False $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION $no_auction -v VAT_INCLUDED:False $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION  \$DFS_QUALIFICATION $params")
            shell("$robotWrapper $contractsign $defaultArgs -i modify_contract_vat_to_true -i modify_contract_amount_net -i modify_contract_value -i modify_contract_invalid_amountNet -i modify_contract_invalid_amount_tender_vat_false $params")
            shell("$robotWrapper $contractmanagement $defaultArgs -i change_contract_amountNet -i change_contract_amount -i change_amount_paid $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs $fast_auction \$EDR_PRE_QUALIFICATION $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION \$DFS_QUALIFICATION $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell("$robotWrapper $contractmanagement $defaultArgs $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION $no_auction $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION $no_auction -v VAT_INCLUDED:False $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION $no_auction -v VAT_INCLUDED:False $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs $fast_auction $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs $fast_auction \$EDR_PRE_QUALIFICATION $params")
            shell("$robotWrapper $auction_short $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION \$DFS_QUALIFICATION $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs $no_auction $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs $no_auction $params")
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
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openeu\":{\"tender\":[1,7.5],\"accelerator\":5760}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION $no_auction $accelerate_openeu $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION \$DFS_QUALIFICATION $params")
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
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openua\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $openProcedure $defaultArgs $no_auction $accelerate_openua $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION \$DFS_QUALIFICATION $params")
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
        String accelerate_open_competitive_dialogue = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,15],\"accelerator\":2880}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION $no_auction $accelerate_open_competitive_dialogue $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION \$DFS_QUALIFICATION $params")
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
        String accelerate_open_competitive_dialogue = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,15],\"accelerator\":2880}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION $no_auction $accelerate_open_competitive_dialogue $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION \$DFS_QUALIFICATION $params")
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

        String defaultArgs = "-A robot_tests_arguments/esco_dfs.txt"
        String accelerate_open_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_esco\":{\"enquiry\":[0,1],\"tender\":[1,7.5],\"accelerator\":5760}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION $no_auction $accelerate_open_esco $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION \$DFS_QUALIFICATION $params")
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
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openeu\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs $accelerate_openeu $params")
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
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openeu\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs $accelerate_openeu $params")
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
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openeu\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs $accelerate_openeu $params")
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
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openeu\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints  $defaultArgs $accelerate_openeu $params")
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
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openeu\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints  $defaultArgs $accelerate_openeu $params")
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
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openua\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $complaints  $defaultArgs $no_auction $accelerate_openua $params")
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
         String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openua\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $complaints  $defaultArgs $no_auction $accelerate_openua $params")
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
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openua\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $complaints  $defaultArgs $no_auction $accelerate_openua $params")
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
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openua\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $complaints  $defaultArgs $no_auction $accelerate_openua $params")
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
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openua\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $complaints $defaultArgs $no_auction $accelerate_openua $params")
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
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openeu\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs $accelerate_openeu $params")
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
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openeu\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs $accelerate_openeu $params")
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
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openeu\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs $accelerate_openeu $params")
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
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openeu\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs $accelerate_openeu $params")
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
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openeu\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs $accelerate_openeu $params")
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
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openeu\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs $accelerate_openeu $params")
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
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openeu\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs $accelerate_openeu $params")
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
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openeu\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs $accelerate_openeu $params")
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
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openeu\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs $accelerate_openeu $params")
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
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openeu\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs $accelerate_openeu $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_openeu_complaint_cancel_tender_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера задоволена та виконана Замовником")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_resolved.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openeu\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs $accelerate_openeu $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_openeu_complaint_cancel_tender_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера створена помилково")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_mistaken.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openeu\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs $accelerate_openeu $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_openeu_complaint_cancel_tender_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера залишена без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_invalid.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openeu\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs $accelerate_openeu $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_openeu_complaint_cancel_tender_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера відхилена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_declined.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openeu\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs $accelerate_openeu $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_openeu_complaint_cancel_tender_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера розгляд зупинено")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_stopped.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openeu\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs $accelerate_openeu $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_openeu_complaint_cancel_lot_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота задоволена та виконана Замовником")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_resolved.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openeu\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs $accelerate_openeu $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_openeu_complaint_cancel_lot_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота створена помилково")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_mistaken.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openeu\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs $accelerate_openeu $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_openeu_complaint_cancel_lot_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота залишена без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_invalid.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openeu\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs $accelerate_openeu $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_openeu_complaint_cancel_lot_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота відхилена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_declined.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openeu\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs $accelerate_openeu $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_openeu_complaint_cancel_lot_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота розгляд зупинено")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_stopped.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openeu\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs $accelerate_openeu $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_aboveThresholdUA_24_hours_award") {
        parameters defaultParameters(config)
        description("Сценарій: Повідомлення про невідповідність пропозиції на єтапі кваліфікації")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/24_hours_award.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openua\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $openProcedure  $defaultArgs $no_auction $accelerate_openua $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_aboveThresholdEU_24_hours_qualification") {
        parameters defaultParameters(config)
        description("Сценарій: Повідомлення про невідповідність пропозиції на єтапі пре-кваліфікації")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/24_hours_qual.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openeu\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $openProcedure $defaultArgs $no_auction $accelerate_openeu $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_aboveThresholdUA_alp") {
        parameters defaultParameters(config)
        description("Сценарій: Обгрунтування аномально низької ціни")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/alp.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openua\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $openProcedure $defaultArgs $no_auction $accelerate_openua $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("cancellation_tendering_aboveThresholdEU") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування надпорогової закупівлі з публікацією англійською мовою. Етап active.tendering")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openeu\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $cancellation $defaultArgs -v MODE:openeu $no_auction $accelerate_openeu $params")
            shell(shellRebot)
        }
    }

    job("cancellation_tendering_competitiveDialogueEU") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування закупівлі конкурентний діалог з публікацією англійською мовою. Етап active.tendering")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation.txt"
        String accelerate_open_competitive_dialogue = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,5],\"accelerator\":14400}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $cancellation $defaultArgs -v MODE:open_competitive_dialogue $no_auction $accelerate_open_competitive_dialogue $params")
            shell(shellRebot)
        }
    }

    job("cancellation_tendering_aboveThresholdUA") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування надпорогової закупівлі. Етап active.tendering")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openua\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $cancellation $defaultArgs -v MODE:openua $no_auction $accelerate_openua $params")
            shell(shellRebot)
        }
    }

    job("cancellation_reporting") {
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

    job("cancellation_negotiation") {
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

    job("cancellation_negotiation.quick") {
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

     job("cancellation_tendering_esco") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування ESCO закупівлі. Етап active.tendering")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation.txt"
        String accelerate_open_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_esco\":{\"enquiry\":[0,1],\"tender\":[1,5],\"accelerator\":14400}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $cancellation $defaultArgs -v MODE:open_esco -v FUNDING_KIND:budget $no_auction $accelerate_open_esco $params")
            shell(shellRebot)
        }
    }

    job("cancellation_tendering_belowThreshold") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування допорогової закупівлі. Етап active.tendering")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation.txt"
        String accelerate_belowThreshold = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"default\":{\"enquiry\":[0,1],\"tender\":[0,5],\"accelerator\":14400}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan $params")
            shell("$robotWrapper $cancellation $defaultArgs -e tender_complaintPeriond_stand_still -e tender_cancellation_stand_still -v MODE:belowThreshold $no_auction $accelerate_belowThreshold $params")
            shell(shellRebot)
        }
    }

    job("cancellation_qualification_belowThreshold") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування допорогової закупівлі. Етап active.qualification")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_qualification_below.txt"
        String accelerate_belowThreshold = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"default\":{\"enquiry\":[1,3],\"tender\":[1,5],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan $params")
            shell("$robotWrapper $cancellation $defaultArgs $no_auction $accelerate_belowThreshold $params")
            shell(shellRebot)
        }
    }

    job("cancellation_qualification_esco") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування ESCO закупівлі. Етап active.qualification")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_qualification_esco.txt"
        String accelerate_open_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_esco\":{\"enquiry\":[0,1],\"tender\":[1,7.5],\"accelerator\":5760}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $cancellation $defaultArgs $no_auction $accelerate_open_esco $params")
            shell(shellRebot)
        }
    }

    job("cancellation_qualification_aboveThresholdUA_defence") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування процедури для потреб оборони. Етап active.qualification")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_qualification_openua_defense.txt"
        String accelerate_openua_defense = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openua_defense\":{\"enquiry\":[0,3],\"tender\":[1,5],\"accelerator\":4320}}}}'"

         steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA.defense $params")
            shell("$robotWrapper $cancellation $defaultArgs $no_auction $accelerate_openua_defense $params")
            shell(shellRebot)
         }
    }

    job("cancellation_qualification_aboveThresholdUA") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування надпорогової закупівлі. Етап active.qualification")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_qualification_openua.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openua\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $cancellation $defaultArgs $no_auction $accelerate_openua $params")
            shell(shellRebot)
        }
    }

    job("cancellation_qualification_aboveThresholdEU") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування надпорогової закупівлі з публікацією англійською мовою. Етап active.qualification")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_qualification_openeu.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openeu\":{\"tender\":[1,7.5],\"accelerator\":5760}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $cancellation $defaultArgs $no_auction $accelerate_openeu $params")
            shell(shellRebot)
        }
    }

    job("cancellation_qualification_closeFrameworkAgreementUA_tender") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування тендера Рамкова угода 1-й етап. Етап active.qualification")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_qualification_framework_agreement.txt"
        String accelerate_open_framework = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_framework\":{\"tender\":[1,7.5],\"accelerator\":5760}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $cancellation $defaultArgs -e lot_cancellation -e lot_cancellation_stand_still -e lot_cancellation_view $no_auction $accelerate_open_framework $params")
            shell(shellRebot)
        }
    }

    job("cancellation_qualification_closeFrameworkAgreementUA_lot") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування лота Рамкова угода 1-й етап. Етап active.qualification")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_qualification_framework_agreement.txt"
        String accelerate_open_framework = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_framework\":{\"tender\":[1,7.5],\"accelerator\":5760}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $cancellation $defaultArgs -e tender_cancellation -e tender_cancellation_stand_still -e tender_cancellation_view $no_auction $accelerate_open_framework $params")
            shell(shellRebot)
        }
    }

    job("cancellation_qualification_closeFrameworkAgreementSelectionUA_tender") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування тендера Рамкова угода 2-й етап. Етап active.qualification")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_qualification_framework_selection.txt"
        String accelerate_open_framework = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_framework\":{\"tender\":[1,7.5],\"accelerator\":5760}}}}'"
        String framework_short_Args = "-A robot_tests_arguments/framework_agreement_short.txt"
        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $openProcedure $framework_short_Args $no_auction $accelerate_open_framework $params")
            shell("$robotWrapper $qualification $framework_short_Args $params")
            shell("$robotWrapper $contractsign $framework_short_Args $params")
            shell("$robotWrapper $agreement $framework_short_Args $params")
            shell("$robotWrapper $cancellation $defaultArgs -e lot_cancellation -e lot_cancellation_stand_still -e lot_cancellation_view $no_auction $params")
            shell(shellRebot)
        }
    }

    job("cancellation_qualification_closeFrameworkAgreementSelectionUA_lot") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування лота Рамкова угода 2-й етап. Етап active.qualification")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_qualification_framework_selection.txt"
        String accelerate_open_framework = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_framework\":{\"tender\":[1,7.5],\"accelerator\":5760}}}}'"
        String framework_short_Args = "-A robot_tests_arguments/framework_agreement_short.txt"
        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $openProcedure $framework_short_Args $no_auction $accelerate_open_framework $params")
            shell("$robotWrapper $qualification $framework_short_Args $params")
            shell("$robotWrapper $contractsign $framework_short_Args $params")
            shell("$robotWrapper $agreement $framework_short_Args $params")
            shell("$robotWrapper $cancellation $defaultArgs -e tender_cancellation -e tender_cancellation_stand_still -e tender_cancellation_view $no_auction $params")
            shell(shellRebot)
        }
    }

    job("cancellation_awarded_belowThreshold") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування допорогової закупівлі. Етап active.awarded")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_awarded_below.txt"
        String accelerate_belowThreshold = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"default\":{\"enquiry\":[1,3],\"tender\":[1,5],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan $params")
            shell("$robotWrapper $cancellation $defaultArgs $no_auction $accelerate_belowThreshold $params")
            shell(shellRebot)
        }
    }

    job("cancellation_awarded_esco") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування ESCO закупівлі. Етап active.awarded")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_awarded_esco.txt"
        String accelerate_open_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_esco\":{\"enquiry\":[0,1],\"tender\":[1,7.5],\"accelerator\":5760}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $cancellation $defaultArgs $no_auction $accelerate_open_esco $params")
            shell(shellRebot)
        }
    }

    job("cancellation_awarded_aboveThresholdUA") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування надпорогової закупівлі. Етап active.awarded")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_awarded_openua.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openua\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $cancellation $defaultArgs $no_auction $accelerate_openua $params")
            shell(shellRebot)
        }
    }

    job("cancellation_awarded_aboveThresholdEU") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування надпорогової закупівлі з публікацією англійською мовою. Етап active.awarded")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_awarded_openeu.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openeu\":{\"tender\":[1,7.5],\"accelerator\":5760}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $cancellation $defaultArgs $no_auction $accelerate_openeu $params")
            shell(shellRebot)
        }
    }

    job("cancellation_awarded_aboveThresholdUA_defence") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування процедури для потреб оборони. Етап active.awarded")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_awarded_openua_defense.txt"
        String accelerate_openua_defense = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openua_defense\":{\"enquiry\":[0,3],\"tender\":[1,5],\"accelerator\":4320}}}}'"

         steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA.defense $params")
            shell("$robotWrapper $cancellation $defaultArgs $no_auction $accelerate_openua_defense $params")
            shell(shellRebot)
         }
    }

    job("cancellation_awarded_closeFrameworkAgreementUA_tender") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування тендера Рамкова угода 1-й етап. Етап active.awarded")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_awarded_framework_agreement.txt"
        String accelerate_open_framework = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_framework\":{\"tender\":[1,7.5],\"accelerator\":5760}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $cancellation $defaultArgs -e lot_cancellation -e lot_cancellation_stand_still -e lot_cancellation_view $no_auction $accelerate_open_framework $params")
            shell(shellRebot)
        }
    }

    job("cancellation_awarded_closeFrameworkAgreementUA_lot") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування лота Рамкова угода 1-й етап. Етап active.awarded")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_awarded_framework_agreement.txt"
        String accelerate_open_framework = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_framework\":{\"tender\":[1,7.5],\"accelerator\":5760}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $cancellation $defaultArgs -e tender_cancellation -e tender_cancellation_stand_still -e tender_cancellation_view $no_auction $accelerate_open_framework $params")
            shell(shellRebot)
        }
    }

    job("cancellation_pre_qualification_esco") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування ESCO закупівлі. Етап active.pre-qualification")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_pre_qualification_esco.txt"
        String accelerate_open_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_esco\":{\"enquiry\":[0,1],\"tender\":[1,7.5],\"accelerator\":5760}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $cancellation $defaultArgs $no_auction $accelerate_open_esco $params")
            shell(shellRebot)
        }
    }

    job("cancellation_pre_qualification_aboveThresholdEU") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування надпорогової закупівлі з публікацією англійською мовою. Етап active.pre-qualification")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_pre_qualification_openeu.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openeu\":{\"tender\":[1,7.5],\"accelerator\":5760}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $cancellation $defaultArgs $no_auction $accelerate_openeu $params")
            shell(shellRebot)
        }
    }

    job("cancellation_pre_qualification_competitiveDialogueUA") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування закупівлі конкурентний діалог. Етап active.pre-qualification")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_pre_qualification_competitive_dialogue_ua.txt"
        String accelerate_open_competitive_dialogue = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,5],\"accelerator\":14400}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $cancellation $defaultArgs $no_auction $accelerate_open_competitive_dialogue $params")
            shell(shellRebot)
        }
    }

    job("cancellation_pre_qualification_competitiveDialogueEU") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування закупівлі конкурентний діалог з публікацією англійською мовою. Етап active.pre-qualification")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_pre_qualification_competitive_dialogue_eu.txt"
        String accelerate_open_competitive_dialogue = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,10],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $cancellation $defaultArgs $no_auction $accelerate_open_competitive_dialogue $params")
            shell(shellRebot)
        }
    }

    job("cancellation_pre_qualification_closeFrameworkAgreementUA_tender") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування тендера Рамкова угода 1-й етап. Етап active.pre-qualification")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_pre_qualification_framework_agreement.txt"
        String accelerate_open_framework = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_framework\":{\"tender\":[1,7.5],\"accelerator\":5760}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $cancellation $defaultArgs -e lot_cancellation -e lot_cancellation_stand_still -e lot_cancellation_view $no_auction $accelerate_open_framework $params")
            shell(shellRebot)
        }
    }

    job("cancellation_pre_qualification_closeFrameworkAgreementUA_lot") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування лота Рамкова угода 1-й етап. Етап active.pre-qualification")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_pre_qualification_framework_agreement.txt"
        String accelerate_open_framework = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_framework\":{\"tender\":[1,7.5],\"accelerator\":5760}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $cancellation $defaultArgs -e tender_cancellation -e tender_cancellation_stand_still -e tender_cancellation_view $no_auction $accelerate_open_framework $params")
            shell(shellRebot)
        }
    }

    job("cancellation_tendering_closeFrameworkAgreementSelectionUA_tender") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування тендера Рамкова угода 2-й етап. Етап active.tendering")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_tendering_framework_selection.txt"
        String accelerate_open_framework = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_framework\":{\"tender\":[1,7.5],\"accelerator\":5760}}}}'"
        String framework_short_Args = "-A robot_tests_arguments/framework_agreement_short.txt"
        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $openProcedure $framework_short_Args $no_auction $accelerate_open_framework $params")
            shell("$robotWrapper $qualification $framework_short_Args $params")
            shell("$robotWrapper $contractsign $framework_short_Args $params")
            shell("$robotWrapper $agreement $framework_short_Args $params")
            shell("$robotWrapper $cancellation $defaultArgs -e lot_cancellation -e lot_cancellation_stand_still -e lot_cancellation_view $no_auction $params")
            shell(shellRebot)
        }
    }

    job("cancellation_tendering_closeFrameworkAgreementSelectionUA_lot") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування лота Рамкова угода 2-й етап. Етап active.tendering")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_tendering_framework_selection.txt"
        String accelerate_open_framework = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_framework\":{\"tender\":[1,7.5],\"accelerator\":5760}}}}'"
        String framework_short_Args = "-A robot_tests_arguments/framework_agreement_short.txt"
        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $openProcedure $framework_short_Args $no_auction $accelerate_open_framework $params")
            shell("$robotWrapper $qualification $framework_short_Args $params")
            shell("$robotWrapper $contractsign $framework_short_Args $params")
            shell("$robotWrapper $agreement $framework_short_Args $params")
            shell("$robotWrapper $cancellation $defaultArgs -e tender_cancellation -e tender_cancellation_stand_still -e tender_cancellation_view $no_auction $params")
            shell(shellRebot)
        }
    }

    job("cancellation_tendering_aboveThresholdUA_defence") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування процедури для потреб оборони. Етап active.tendering")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_tendering_openua_defense.txt"
        String accelerate_openua_defense = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openua_defense\":{\"enquiry\":[0,3],\"tender\":[1,5],\"accelerator\":4320}}}}'"

         steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA.defense $params")
            shell("$robotWrapper $cancellation $defaultArgs $no_auction $accelerate_openua_defense $params")
            shell(shellRebot)
         }
    }

    job("cancellation_tendering_competitiveDialogueEU_stage2") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування закупівлі конкурентний діалог з публікацією англійською мовою. Етап active.tendering_stage2")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_tendering_competitive_dialogue_eu_stage2.txt"
        String accelerate_open_competitive_dialogue = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,10],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $cancellation $defaultArgs -v MODE:open_competitive_dialogue $no_auction $accelerate_open_competitive_dialogue $params")
            shell(shellRebot)
        }
    }

    job("cancellation_tendering_competitiveDialogueUA_stage2") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування закупівлі конкурентний діалог. Етап active.tendering_stage2")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_tendering_competitive_dialogue_ua_stage2.txt"
        String accelerate_open_competitive_dialogue = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,15],\"accelerator\":2880}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $cancellation $defaultArgs -v MODE:open_competitive_dialogue -v DIALOGUE_TYPE:UA $no_auction $accelerate_open_competitive_dialogue $params")
            shell(shellRebot)
        }
    }

    job("cancellation_enquiry_belowThreshold") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування допорогової закупівлі. Етап active.enquiry")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_enquiry_below.txt"
        String accelerate_belowThreshold = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"default\":{\"enquiry\":[1,5],\"tender\":[1,5],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan $params")
            shell("$robotWrapper $cancellation $defaultArgs -e tender_complaintPeriond_stand_still -e tender_cancellation_stand_still -v MODE:belowThreshold $no_auction $accelerate_belowThreshold $params")
            shell(shellRebot)
        }
    }

    job("cancellation_enquiry_closeFrameworkAgreementSelectionUA_tender") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування тендера Рамкова угода 2-й етап. Етап active.enquiry")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_enquiry_framework_selection.txt"
        String accelerate_open_framework = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_framework\":{\"tender\":[1,7.5],\"accelerator\":5760}}}}'"
        String framework_short_Args = "-A robot_tests_arguments/framework_agreement_short.txt"
        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $openProcedure $framework_short_Args $no_auction $accelerate_open_framework $params")
            shell("$robotWrapper $qualification $framework_short_Args $params")
            shell("$robotWrapper $contractsign $framework_short_Args $params")
            shell("$robotWrapper $agreement $framework_short_Args $params")
            shell("$robotWrapper $cancellation $defaultArgs -e lot_cancellation -e lot_cancellation_stand_still -e lot_cancellation_view $no_auction $params")
            shell(shellRebot)
        }
    }

    job("cancellation_enquiry_closeFrameworkAgreementSelectionUA_lot") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування лота Рамкова угода 2-й етап. Етап active.enquiry")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_enquiry_framework_selection.txt"
        String accelerate_open_framework = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_framework\":{\"tender\":[1,7.5],\"accelerator\":5760}}}}'"
        String framework_short_Args = "-A robot_tests_arguments/framework_agreement_short.txt"
        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $openProcedure $framework_short_Args $no_auction $accelerate_open_framework $params")
            shell("$robotWrapper $qualification $framework_short_Args $params")
            shell("$robotWrapper $contractsign $framework_short_Args $params")
            shell("$robotWrapper $agreement $framework_short_Args $params")
            shell("$robotWrapper $cancellation $defaultArgs -e tender_cancellation -e tender_cancellation_stand_still -e tender_cancellation_view $no_auction $params")
            shell(shellRebot)
        }
    }

     job("cancellation_tendering_closeFrameworkAgreementUA_lot") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування лоту Рамкова угода 1-й етап")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation.txt"
        String accelerate_open_framework = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_framework\":{\"tender\":[1,5],\"accelerator\":14400}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $cancellation $defaultArgs -e tender_cancellation -e tender_cancellation_stand_still -e tender_cancellation_view -v MODE:open_framework -v NUMBER_OF_LOTS:1 $no_auction $accelerate_open_framework $params")
            shell(shellRebot)
        }
    }

    job("cancellation_tendering_closeFrameworkAgreementUA_tender") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування тендера Рамкова угода 1-й етап. Етап active.tendering")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation.txt"
        String accelerate_open_framework = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_framework\":{\"tender\":[1,5],\"accelerator\":14400}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $cancellation $defaultArgs -e lot_cancellation -e lot_cancellation_stand_still -e lot_cancellation_view -v MODE:open_framework -v NUMBER_OF_LOTS:1 $no_auction $accelerate_open_framework $params")
            shell(shellRebot)
        }
    }

    job("cancellation_tendering_competitiveDialogueUA") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування закупівлі (competitive dialogue UA) 1 етап. Етап active.tendering")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation.txt"
        String accelerate_open_competitive_dialogue = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,5],\"accelerator\":14400}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $cancellation $defaultArgs -v MODE:open_competitive_dialogue -v DIALOGUE_TYPE:UA $no_auction $accelerate_open_competitive_dialogue $params")
            shell(shellRebot)
        }
    }

    job("cancellation_pre_qualification_competitiveDialogueEU_stage2") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування закупівлі конкурентний діалог з публікацією англійською мовою. Етап active.pre_qualification_stage2")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_pre_qualification_competitive_dialogue_eu_stage2.txt"
        String accelerate_open_competitive_dialogue = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,10],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $cancellation $defaultArgs $no_auction $accelerate_open_competitive_dialogue $params")
            shell(shellRebot)
        }
    }

    job("cancellation_qualification_competitiveDialogueEU_stage2") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування закупівлі конкурентний діалог з публікацією англійською мовою. Етап active.qualification_stage2")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_qualification_competitive_dialogue_eu_stage2.txt"
        String accelerate_open_competitive_dialogue = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,10],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $cancellation $defaultArgs $no_auction $accelerate_open_competitive_dialogue $params")
            shell(shellRebot)
        }
    }

    job("cancellation_qualification_competitiveDialogueUA_stage2") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування закупівлі конкурентний діалог. Етап active.qualification_stage2")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_qualification_competitive_dialogue_ua_stage2.txt"
        String accelerate_open_competitive_dialogue = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,15],\"accelerator\":2880}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $cancellation $defaultArgs $no_auction $accelerate_open_competitive_dialogue $params")
            shell(shellRebot)
        }
    }

    job("cancellation_awarded_closeFrameworkAgreementSelectionUA_tender") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування тендера Рамкова угода 2-й етап. Етап active.awarded")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_awarded_framework_selection.txt"
        String accelerate_open_framework = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_framework\":{\"tender\":[1,7.5],\"accelerator\":5760}}}}'"
        String framework_short_Args = "-A robot_tests_arguments/framework_agreement_short.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $openProcedure $framework_short_Args $no_auction $accelerate_open_framework $params")
            shell("$robotWrapper $qualification $framework_short_Args $params")
            shell("$robotWrapper $contractsign $framework_short_Args $params")
            shell("$robotWrapper $agreement $framework_short_Args $params")
            shell("$robotWrapper $cancellation $defaultArgs -e lot_cancellation -e lot_cancellation_stand_still -e lot_cancellation_view $no_auction $params")
            shell(shellRebot)
        }
    }

    job("cancellation_awarded_closeFrameworkAgreementSelectionUA_lot") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування лота Рамкова угода 2-й етап. Етап active.awarded")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_awarded_framework_selection.txt"
        String accelerate_open_framework = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_framework\":{\"tender\":[1,7.5],\"accelerator\":5760}}}}'"
        String framework_short_Args = "-A robot_tests_arguments/framework_agreement_short.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $openProcedure $framework_short_Args $no_auction $accelerate_open_framework $params")
            shell("$robotWrapper $qualification $framework_short_Args $params")
            shell("$robotWrapper $contractsign $framework_short_Args $params")
            shell("$robotWrapper $agreement $framework_short_Args $params")
            shell("$robotWrapper $cancellation $defaultArgs -e tender_cancellation -e tender_cancellation_stand_still -e tender_cancellation_view $no_auction $accelerate_open_framework $params")
            shell(shellRebot)
        }
    }

    job("cancellation_awarded_competitiveDialogueUA_stage2") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування закупівлі конкурентний діалог. Етап active.awarded_stage2")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_awarded_competitive_dialogue_ua_stage2.txt"
        String accelerate_open_competitive_dialogue = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,15],\"accelerator\":2880}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $cancellation $defaultArgs $no_auction $accelerate_open_competitive_dialogue $params")
            shell(shellRebot)
        }
    }

    job("cancellation_awarded_competitiveDialogueEU_stage2") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування закупівлі конкурентний діалог з публікацією англійською мовою. Етап active.awarded_stage2")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_awarded_competitive_dialogue_eu_stage2.txt"
        String accelerate_open_competitive_dialogue = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,10],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $cancellation $defaultArgs $no_auction $accelerate_open_competitive_dialogue $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_priceQuotation_full") {
        parameters defaultParameters(config)
        description("Сценарій: Процедура PQ від початку до кінця")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/priceQuotation.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v mode:priceQuotation $params")
            shell("$robotWrapper $priceQuotation $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_priceQuotation_cancellation_tendering") {
        parameters defaultParameters(config)
        description("Сценарій: Процедура PQ скасування під час етапу подачі пропозицій")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/priceQuotation_cancellation_after_active_tendering.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v mode:priceQuotation $params")
            shell("$robotWrapper $priceQuotation $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_priceQuotation_cancellation_qualification") {
        parameters defaultParameters(config)
        description("Сценарій: Процедура PQ скасування під час етапу визначення переможця")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/priceQuotation_cancellation_after_active_qualification.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v mode:priceQuotation $params")
            shell("$robotWrapper $priceQuotation $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_priceQuotation_cancellation_awarded") {
        parameters defaultParameters(config)
        description("Сценарій: Процедура PQ скасування під час етапу укладання контракту")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/priceQuotation_cancellation_after_active_award.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v mode:priceQuotation $params")
            shell("$robotWrapper $priceQuotation $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_priceQuotation_cancelled_without_bids") {
        parameters defaultParameters(config)
        description("Сценарій: Процедура PQ скасування через відсутність пропозицій")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/priceQuotation_cancelled_without_bids.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v mode:priceQuotation $params")
            shell("$robotWrapper $priceQuotation $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_priceQuotation_unsuccessful") {
        parameters defaultParameters(config)
        description("Сценарій: Процедура PQ скасовано.дискваліфіковано всіх учасників")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/priceQuotation_unsuccessful.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v mode:priceQuotation $params")
            shell("$robotWrapper $priceQuotation $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_priceQuotation_shortlistedfirms_empty") {
        parameters defaultParameters(config)
        description("Сценарій: Створити роцедуру PQ - shortlistedfirms порожній")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/priceQuotation_bot_unsuccessful_shortlistedfirms_empty.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v mode:priceQuotation $params")
            shell("$robotWrapper $priceQuotation $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_priceQuotation_hidden_profile") {
        parameters defaultParameters(config)
        description("Сценарій: Створити роцедуру PQ - статус профіля hidden")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/priceQuotation_bot_unsuccessful_hidden_profile.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v mode:priceQuotation $params")
            shell("$robotWrapper $priceQuotation $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_priceQuotation_unknown_profile") {
        parameters defaultParameters(config)
        description("Сценарій: Створити роцедуру PQ - неіснуючий профіль")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/priceQuotation_bot_unsuccessful_unknown_profile.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v mode:priceQuotation $params")
            shell("$robotWrapper $priceQuotation $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_priceQuotation_bot_unsuccessful") {
        parameters defaultParameters(config)
        description("Сценарій: Створити роцедуру PQ - невірний профіль")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/priceQuotation_bot_unsuccessful.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v mode:priceQuotation $params")
            shell("$robotWrapper $priceQuotation $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_priceQuotation_negative") {
        parameters defaultParameters(config)
        description("Сценарій: Створити роцедуру PQ - негативні тести")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/priceQuotation_negative.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v mode:priceQuotation $params")
            shell("$robotWrapper $priceQuotation $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_priceQuotation_negative_draft") {
        parameters defaultParameters(config)
        description("Сценарій: Змінити роцедуру PQ - негативні тести")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/priceQuotation_negative_draft.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v mode:priceQuotation $params")
            shell("$robotWrapper $priceQuotation $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("old_auction_belowThreshold") {
        parameters defaultParameters(config)
        description("Сценарій: Допорогова закупівля. Старий модуль аукціона.")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/below_simple.txt"
        String accelerate_belowThreshold = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"default\":{\"enquiry\":[1,5],\"tender\":[1,5],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan $params")
            shell("$robotWrapper $openProcedure $defaultArgs $accelerate_belowThreshold $params")
            shell("$robotWrapper $old_auction $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("old_auction_aboveThresholdEU") {
        parameters defaultParameters(config)
        description("Сценарій: Надпорогова закупівля з публікацією англійською мовою. Старий модуль аукціона.")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/openeu_simple.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openeu\":{\"tender\":[1,7.5],\"accelerator\":5760}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $openProcedure $defaultArgs $accelerate_openeu \$EDR_PRE_QUALIFICATION $params")
            shell("$robotWrapper $old_auction $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("old_auction_aboveThresholdUA") {
        parameters defaultParameters(config)
        description("Сценарій: Надпорогова закупівля. Старий модуль аукціона.")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/openua_simple.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openua\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $openProcedure $defaultArgs $accelerate_openua $params")
            shell("$robotWrapper $old_auction $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("old_auction_esco") {
        parameters defaultParameters(config)
        description("Сценарій: Відкриті торги для закупівлі енергосервісу. Старий модуль аукціона.")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/esco_simple.txt"
        String accelerate_open_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_esco\":{\"enquiry\":[0,1],\"tender\":[1,7.5],\"accelerator\":5760}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION $accelerate_open_esco $params")
            shell("$robotWrapper $auction_short $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("old_auction_competitiveDialogueUA") {
        parameters defaultParameters(config)
        description("Сценарій: Конкурентний діалог для надпорогових закупівель українською мовою. Старий модуль аукціона.")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/competitive_dialogue_simple_UA.txt"
        String accelerate_open_competitive_dialogue = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,15],\"accelerator\":2880}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION $accelerate_open_competitive_dialogue $params")
            shell("$robotWrapper $old_auction $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("old_auction_competitiveDialogueEU") {
        parameters defaultParameters(config)
        description("Сценарій: Конкурентний діалог з публікацією англійською мовою. Старий модуль аукціона.")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/competitive_dialogue_simple.txt"
        String accelerate_open_competitive_dialogue = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,15],\"accelerator\":2880}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION $accelerate_open_competitive_dialogue $params")
            shell("$robotWrapper $old_auction $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("old_auction_framework") {
        parameters defaultParameters(config)
        description("Сценарій: Рамкова угода. Старий модуль аукціона.")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 18000)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/framework_agreement_simple.txt"
        String accelerate_open_framework = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_framework\":{\"tender\":[1,7.5],\"accelerator\":5760}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION $accelerate_open_framework $params")
            shell("$robotWrapper $auction_short $defaultArgs -i auction $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION \$DFS_QUALIFICATION $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell("$robotWrapper $agreement $defaultArgs $params")
            shell("$robotWrapper $selection -A robot_tests_arguments/framework_selection_simple.txt $params")
            shell("$robotWrapper -o auction_short_framework_output.xml -s auction -A robot_tests_arguments/framework_selection_full.txt $params")
            shell(shellRebot)
        }
    }

    job("old_auction_aboveThresholdUA_defence") {
        parameters defaultParameters(config)
        description("Сценарій: Надпорогова закупівля. Переговорна процедура для потреб оборони (з одним учасником). Старий модуль аукціона.")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/openUAdefense_one_bid.txt"
        String accelerate_openua_defense = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openua_defense\":{\"enquiry\":[0,3],\"tender\":[1,5],\"accelerator\":4320}}}}'"

         steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA.defense $params")
            shell("$robotWrapper $openProcedure $defaultArgs $accelerate_openua_defense $params")
            shell("$robotWrapper $old_auction $defaultArgs $params")
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
                    "${config.environment}_aboveThresholdEU_vat_true_false",
                    "${config.environment}_aboveThresholdEU_vat_false_false",
                    "${config.environment}_aboveThresholdEU_vat_false_true",
                    "${config.environment}_aboveThresholdUA",
                    "${config.environment}_frameworkagreement",
                    "${config.environment}_aboveThresholdUA_vat_true_false",
                    "${config.environment}_aboveThresholdUA_vat_false_false",
                    "${config.environment}_aboveThresholdUA_vat_false_true",
                    "${config.environment}_below_funders_full",
                    "${config.environment}_belowThreshold",
                    "${config.environment}_belowThreshold_vat_true_false",
                    "${config.environment}_belowThreshold_vat_false_false",
                    "${config.environment}_belowThreshold_vat_false_true",
                    "${config.environment}_competitiveDialogueEU",
                    "${config.environment}_competitiveDialogueEU_stage1",
                    "${config.environment}_competitiveDialogueEU_vat_true_false",
                    "${config.environment}_competitiveDialogueEU_vat_false_false",
                    "${config.environment}_competitiveDialogueEU_vat_false_true",
                    "${config.environment}_competitiveDialogueUA",
                    "${config.environment}_competitiveDialogueUA_vat_true_false",
                    "${config.environment}_competitiveDialogueUA_vat_false_false",
                    "${config.environment}_competitiveDialogueUA_vat_false_true",
                    "${config.environment}_sb_negotiation",
                    "${config.environment}_sb_negotiation.quick",
                    "${config.environment}_sb_reporting",
                    "${config.environment}_feed_reading",
                    "${config.environment}_single_item_tender",
                    "${config.environment}_aboveThresholdUA_defence_one_bid",
                    "${config.environment}_esco",
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
                    "${config.environment}_openeu_complaint_cancel_tender_resolved",
                    "${config.environment}_openeu_complaint_cancel_tender_mistaken",
                    "${config.environment}_openeu_complaint_cancel_tender_invalid",
                    "${config.environment}_openeu_complaint_cancel_tender_declined",
                    "${config.environment}_openeu_complaint_cancel_tender_stopped",
                    "${config.environment}_openeu_complaint_cancel_lot_resolved",
                    "${config.environment}_openeu_complaint_cancel_lot_mistaken",
                    "${config.environment}_openeu_complaint_cancel_lot_invalid",
                    "${config.environment}_openeu_complaint_cancel_lot_declined",
                    "${config.environment}_openeu_complaint_cancel_lot_stopped",
                    "${config.environment}_aboveThresholdUA_24_hours_award",
                    "${config.environment}_aboveThresholdEU_24_hours_qualification",
                    "${config.environment}_aboveThresholdUA_alp",
                    "${config.environment}_priceQuotation_full",
                    "${config.environment}_priceQuotation_cancellation_tendering",
                    "${config.environment}_priceQuotation_cancellation_qualification",
                    "${config.environment}_priceQuotation_cancellation_awarded",
                    "${config.environment}_priceQuotation_cancelled_without_bids",
                    "${config.environment}_priceQuotation_unsuccessful",
                    "${config.environment}_priceQuotation_shortlistedfirms_empty",
                    "${config.environment}_priceQuotation_hidden_profile",
                    "${config.environment}_priceQuotation_unknown_profile",
                    "${config.environment}_priceQuotation_bot_unsuccessful",
                    "${config.environment}_priceQuotation_negative",
                    "${config.environment}_priceQuotation_negative_draft",
                    "${config.environment}_aboveThresholdEU_DFS",
                    "${config.environment}_aboveThresholdUA_DFS",
                    "${config.environment}_competitiveDialogueEU_DFS",
                    "${config.environment}_competitiveDialogueUA_DFS",
                    "${config.environment}_esco_DFS",
                ]
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

    listView("cancellation") {
        description('Cancellation for all procedure types in all tender statuses')
        jobs {
            names("cancellation",
                    "cancellation_enquiry_belowThreshold",
                    "cancellation_enquiry_closeFrameworkAgreementSelectionUA_tender",
                    "cancellation_enquiry_closeFrameworkAgreementSelectionUA_lot",
                    "cancellation_tendering_esco",
                    "cancellation_tendering_aboveThresholdUA_defence",
                    "cancellation_tendering_belowThreshold",
                    "cancellation_tendering_competitiveDialogueUA",
                    "cancellation_tendering_aboveThresholdUA",
                    "cancellation_tendering_competitiveDialogueEU",
                    "cancellation_tendering_aboveThresholdEU",
                    "cancellation_tendering_closeFrameworkAgreementUA_tender",
                    "cancellation_tendering_closeFrameworkAgreementUA_lot",
                    "cancellation_tendering_closeFrameworkAgreementSelectionUA_tender",
                    "cancellation_tendering_closeFrameworkAgreementSelectionUA_lot",
                    "cancellation_tendering_competitiveDialogueEU_stage2",
                    "cancellation_tendering_competitiveDialogueUA_stage2",
                    "cancellation_negotiation.quick",
                    "cancellation_negotiation",
                    "cancellation_reporting",
                    "cancellation_pre_qualification_esco",
                    "cancellation_pre_qualification_aboveThresholdEU",
                    "cancellation_pre_qualification_competitiveDialogueUA",
                    "cancellation_pre_qualification_closeFrameworkAgreementUA_tender",
                    "cancellation_pre_qualification_closeFrameworkAgreementUA_lot",
                    "cancellation_pre_qualification_competitiveDialogueEU",
                    "cancellation_pre_qualification_competitiveDialogueEU_stage2",
                    "cancellation_qualification_belowThreshold",
                    "cancellation_qualification_esco",
                    "cancellation_qualification_aboveThresholdUA_defence",
                    "cancellation_qualification_aboveThresholdUA",
                    "cancellation_qualification_aboveThresholdEU",
                    "cancellation_qualification_closeFrameworkAgreementSelectionUA_tender",
                    "cancellation_qualification_closeFrameworkAgreementSelectionUA_lot",
                    "cancellation_qualification_closeFrameworkAgreementUA_tender",
                    "cancellation_qualification_closeFrameworkAgreementUA_lot",
                    "cancellation_qualification_competitiveDialogueUA_stage2",
                    "cancellation_qualification_competitiveDialogueEU_stage2",
                    "cancellation_awarded_belowThreshold",
                    "cancellation_awarded_esco",
                    "cancellation_awarded_aboveThresholdUA",
                    "cancellation_awarded_aboveThresholdEU",
                    "cancellation_awarded_aboveThresholdUA_defence",
                    "cancellation_awarded_closeFrameworkAgreementUA_tender",
                    "cancellation_awarded_closeFrameworkAgreementUA_lot",
                    "cancellation_awarded_closeFrameworkAgreementSelectionUA_tender",
                    "cancellation_awarded_closeFrameworkAgreementSelectionUA_lot",
                    "cancellation_awarded_competitiveDialogueUA_stage2",
                    "cancellation_awarded_competitiveDialogueEU_stage2",)
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

multiJob("cancellation") {
    description('my description')
    parameters {
        choiceParam('BRANCH', ['master', 'dev_prozorro_2', 'dev_prozorro'], 'my description')
        stringParam('RELEASE_NAME', 'main', 'my description')
        choiceParam('API_HOST_URL', ['http://api.${RELEASE_NAME}.k8s.prozorro.gov.ua', 'https://lb-api-staging.prozorro.gov.ua', 'https://lb-api-sandbox.prozorro.gov.ua', 'https://lb-api-sandbox-2.prozorro.gov.ua'], 'my description')
        choiceParam('API_VERSION', ['2.5', '2.4',], 'my description')
        choiceParam('EDR_HOST_URL', ['https://lb-edr-staging.prozorro.gov.ua', 'https://lb-edr-sandbox.prozorro.gov.ua', 'https://lb-edr-sandbox-2.prozorro.gov.ua'], 'my description')
        choiceParam('EDR_VERSION', ['1.0', '0'], 'my description')
        choiceParam('DS_HOST_URL', ['https://upload-docs-staging.prozorro.gov.ua', 'https://upload-docs-sandbox.prozorro.gov.ua', 'https://upload-docs-sandbox-2.prozorro.gov.ua', 'http://ds.k8s.prozorro.gov.ua'], 'my description')
        choiceParam('DS_REGEXP', ["^http?:\\/\\/ds\\.k8s\\.prozorro\\.gov\\.ua\\/get\\/([0-9A-Fa-f]{32})",
        "^https?:\\/\\/public-docs(?:-sandbox)?\\.prozorro\\.gov\\.ua\\/get\\/([0-9A-Fa-f]{32})",
        "^https?:\\/\\/public-docs(?:-staging)?\\.prozorro\\.gov\\.ua\\/get\\/([0-9A-Fa-f]{32})",
        "^https?:\\/\\/public-docs(?:-sandbox-2)?\\.prozorro\\.gov\\.ua\\/get\\/([0-9A-Fa-f]{32})"], 'my description')
        choiceParam('PAYMENT_API', ['https://integration-staging.prozorro.gov.ua/liqpay', 'https://integration-sandbox-2.prozorro.gov.ua/liqpay'], 'my description')
        choiceParam('PAYMENT_API_VERSION', ['v1'], 'my description')
        choiceParam('AUCTION_REGEXP', ["^http?:\\/\\/auctions\\.\${RELEASE_NAME}\\.k8s\\.prozorro\\.gov\\.ua\\/(esco-)?tenders\\/([0-9A-Fa-f]{32})",
        "^https?:\\/\\/auction(?:-sandbox)?\\.prozorro\\.gov\\.ua\\/(esco-)?tenders\\/([0-9A-Fa-f]{32})",
        "^https?:\\/\\/auction(?:-staging)?\\.prozorro\\.gov\\.ua\\/(esco-)?tenders\\/([0-9A-Fa-f]{32})",
        "^https?:\\/\\/auction(?:-sandbox-2)?\\.prozorro\\.gov\\.ua\\/(esco-)?tenders\\/([0-9A-Fa-f]{32})"], 'my description')
    }
        steps {
            phase("Test") {
                def innerJobs = [
                            "cancellation_enquiry_belowThreshold",
                            "cancellation_enquiry_closeFrameworkAgreementSelectionUA_tender",
                            "cancellation_enquiry_closeFrameworkAgreementSelectionUA_lot",
                            "cancellation_tendering_esco",
                            "cancellation_tendering_aboveThresholdUA_defence",
                            "cancellation_tendering_belowThreshold",
                            "cancellation_tendering_competitiveDialogueUA",
                            "cancellation_tendering_aboveThresholdUA",
                            "cancellation_tendering_competitiveDialogueEU",
                            "cancellation_tendering_aboveThresholdEU",
                            "cancellation_tendering_closeFrameworkAgreementSelectionUA_tender",
                            "cancellation_tendering_closeFrameworkAgreementSelectionUA_lot",
                            "cancellation_tendering_competitiveDialogueEU_stage2",
                            "cancellation_tendering_competitiveDialogueUA_stage2",
                            "cancellation_tendering_closeFrameworkAgreementUA_tender",
                            "cancellation_tendering_closeFrameworkAgreementUA_lot",
                            "cancellation_negotiation.quick",
                            "cancellation_negotiation",
                            "cancellation_reporting",
                            "cancellation_pre_qualification_esco",
                            "cancellation_pre_qualification_aboveThresholdEU",
                            "cancellation_pre_qualification_competitiveDialogueUA",
                            "cancellation_pre_qualification_closeFrameworkAgreementUA_tender",
                            "cancellation_pre_qualification_closeFrameworkAgreementUA_lot",
                            "cancellation_pre_qualification_competitiveDialogueEU",
                            "cancellation_pre_qualification_competitiveDialogueEU_stage2",
                            "cancellation_qualification_belowThreshold",
                            "cancellation_qualification_esco",
                            "cancellation_qualification_aboveThresholdUA_defence",
                            "cancellation_qualification_aboveThresholdUA",
                            "cancellation_qualification_aboveThresholdEU",
                            "cancellation_qualification_closeFrameworkAgreementSelectionUA_tender",
                            "cancellation_qualification_closeFrameworkAgreementSelectionUA_lot",
                            "cancellation_qualification_closeFrameworkAgreementUA_tender",
                            "cancellation_qualification_closeFrameworkAgreementUA_lot",
                            "cancellation_qualification_competitiveDialogueUA_stage2",
                            "cancellation_qualification_competitiveDialogueEU_stage2",
                            "cancellation_awarded_belowThreshold",
                            "cancellation_awarded_esco",
                            "cancellation_awarded_aboveThresholdUA",
                            "cancellation_awarded_aboveThresholdEU",
                            "cancellation_awarded_aboveThresholdUA_defence",
                            "cancellation_awarded_closeFrameworkAgreementUA_tender",
                            "cancellation_awarded_closeFrameworkAgreementUA_lot",
                            "cancellation_awarded_closeFrameworkAgreementSelectionUA_tender",
                            "cancellation_awarded_closeFrameworkAgreementSelectionUA_lot",
                            "cancellation_awarded_competitiveDialogueUA_stage2",
                            "cancellation_awarded_competitiveDialogueEU_stage2",
                ]
                innerJobs.each { String scenario -> phaseJob(scenario) {
                    currentJobParameters(true)
                    abortAllJobs(false)
                }}
            }
        }
    }

    listView("old_auction") {
        description('Old auction staging testing')
        jobs {
            names("old_auction",
                "old_auction_belowThreshold",
                "old_auction_aboveThresholdEU",
                "old_auction_aboveThresholdUA",
                "old_auction_esco",
                "old_auction_competitiveDialogueUA",
                "old_auction_competitiveDialogueEU",
                "old_auction_frameworkagreement",
                "old_auction_aboveThresholdUA_defence",)
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

multiJob("old_auction") {
    description('my description')
    triggers {cron("0 6 * * *")}
    parameters {
        stringParam('BRANCH', 'broker', 'my description')
        stringParam('API_HOST_URL', 'https://lb-api-staging.prozorro.gov.ua', 'my description')
        stringParam('API_VERSION', '2.5', 'my description')
        stringParam('EDR_HOST_URL', 'https://lb-edr-staging.prozorro.gov.ua', 'my description')
        stringParam('EDR_VERSION', '1.0', 'my description')
        stringParam('DS_HOST_URL', 'https://upload-docs-staging.prozorro.gov.ua', 'my description')
        stringParam('DS_REGEXP', '^https?:\\/\\/public-docs(?:-staging)?\\.prozorro\\.gov\\.ua\\/get\\/([0-9A-Fa-f]{32})', 'my description')
        stringParam('PAYMENT_API', 'https://integration-staging.prozorro.gov.ua/liqpay', 'my description')
        stringParam('PAYMENT_API_VERSION', 'v1', 'my description')
        stringParam('AUCTION_REGEXP', '^https?:\\/\\/auction(?:-staging)?\\.prozorro\\.gov\\.ua\\/(esco-)?tenders\\/([0-9A-Fa-f]{32})', 'my description')
    }
        steps {
            phase("Test") {
                def innerJobs = [
                "old_auction_belowThreshold",
                "old_auction_aboveThresholdEU",
                "old_auction_aboveThresholdUA",
                "old_auction_esco",
                "old_auction_competitiveDialogueUA",
                "old_auction_competitiveDialogueEU",
                "old_auction_frameworkagreement",
                "old_auction_aboveThresholdUA_defence"
                ]
                innerJobs.each { String scenario -> phaseJob(scenario) {
                    currentJobParameters(true)
                    abortAllJobs(false)
                }}
            }
        }
    }

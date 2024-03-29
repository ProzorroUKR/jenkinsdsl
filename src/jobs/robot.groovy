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

String shellBuildout = "python3 bootstrap.py\nbin/buildout -N buildout:always-checkout=force\nbin/develop update -f\npython3 fix_python3_conflicts.py"
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
String fast_auction = "-v submissionMethodDetails:\"quick(mode:fast-auction)\""
String priceQuotation = "-o priceQuotation_output.xml -s priceQuotationProcedure"
String old_auction = "-o old_auction_output.xml -s auction_long"
String selection_auction_short = "-o auction_short_selection_output.xml -s auction"
String selection_qualification = "-o qualification_selection_output.xml -s qualification"
String selection_contractsign = "-o contract_selection_output.xml -s contract_signing"
String selection_contractmanagement = "-o contract_management_selection_output.xml -s contract_management"
String openeu_acceleration = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
String openua_acceleration = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdUA\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"
String below_acceleration = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"default\":{\"enquiry\":[1,2],\"tender\":[1,7],\"accelerator\":4320}}}}'"
String competitive_dialogue_EU_acceleration = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,10],\"accelerator\":4320}}}}'"
String competitive_dialogue_UA_acceleration = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,10],\"accelerator\":4320}}}}'"
String close_framework_agreement_ua_acceleration = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"closeFrameworkAgreementUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
String claims = "-o claims_output.xml -s claims"

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
                    "EDR_VERSION": "1.0",
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
                    "EDR_VERSION": "1.0",
                    "PAYMENT_API": "https://integration-staging.prozorro.gov.ua/liqpay",
                    "PAYMENT_API_VERSION": "v1",
                    "AUCTION_REGEXP": "^https?:\\/\\/auction(?:-new)?(?:-staging)?\\.prozorro\\.gov\\.ua\\/(esco-)?tenders\\/([0-9A-Fa-f]{32})",
                    "OLD_SANDBOX_AUCTION_REGEXP": "^https?:\\/\\/auction(?:-staging)?\\.prozorro\\.gov\\.ua\\/(esco-)?tenders\\/([0-9A-Fa-f]{32})",
                    "DS_REGEXP": "^https?:\\/\\/public-docs(?:-staging)?\\.prozorro\\.gov\\.ua\\/get\\/([0-9A-Fa-f]{32})",
                ],
                cron: "0 4 * * *",
                branch: "dev_prozorro_2",
                concurrentBuild: false,
                label: "rocky",
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
                    "OLD_SANDBOX_AUCTION_REGEXP": "^https?:\\/\\/auction(?:-sandbox-2-old)?\\.prozorro\\.gov\\.ua\\/(esco-)?tenders\\/([0-9A-Fa-f]{32})",
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

//////////////////////////////////////////////////////////////////////////////////////////////////
//                  PROCEDURE FULL TESTING
//////////////////////////////////////////////////////////////////////////////////////////////////

    job("${config.environment}_aboveThresholdEU") {
        parameters defaultParameters(config)
        description("Сценарій: Надпорогова закупівля з публікацією англійською мовою.")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables {defaultEnv() defaultEnv_dfs()}

        String defaultArgs = "-A robot_tests_arguments/openeu.txt"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $openProcedure $defaultArgs $fast_auction $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell("$robotWrapper $contractmanagement $defaultArgs $params")
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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/openua.txt"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $openProcedure $defaultArgs $fast_auction $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/framework_agreement_full.txt"
        String selectionArgs = "-A robot_tests_arguments/framework_selection_full.txt"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $openProcedure $defaultArgs $fast_auction -v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"default\":{\"tender\":[0,31]}}}}' -v accelerator:2880 $params")
            shell("$robotWrapper $auction_short $defaultArgs -i auction $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell("$robotWrapper $agreement $defaultArgs $params")
            shell("$robotWrapper $selection $selectionArgs $fast_auction $params")
            shell("$robotWrapper $selection_auction_short $selectionArgs $params")
            shell("$robotWrapper $selection_qualification $selectionArgs $params")
            shell("$robotWrapper $selection_contractsign $selectionArgs $params")
            shell("$robotWrapper $selection_contractmanagement $selectionArgs $params")
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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/below_funders_full.txt"

        steps {
            shell(shellBuildout)
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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/below.txt"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan $params")
            shell("$robotWrapper $openProcedure $defaultArgs $fast_auction $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell("$robotWrapper $contractmanagement $defaultArgs $params")
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
            shell("$robotWrapper $planning -i create_plan -i find_plan $params")
            shell("$robotWrapper $openProcedure $defaultArgs $fast_auction $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell("$robotWrapper $contractmanagement $defaultArgs $params")
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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/esco.txt"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $openProcedure $defaultArgs $fast_auction $params")
            shell("$robotWrapper $auction_short $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
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
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $openProcedure $defaultArgs $fast_auction $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs -i modify_contract_invalid_amount -i modify_contract_invalid_amountNet_tender_vat_true -i modify_contract_amount_net -i modify_contract_value $params")
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
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $openProcedure $defaultArgs $fast_auction $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell("$robotWrapper $contractmanagement $defaultArgs $params")
            shell(shellRebot)
        }
    }

    ["negotiation", "negotiation.quick", "reporting"].each { String scenario ->
    job("${config.environment}_$scenario") {
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

    job("${config.environment}_simple_defence") {
        parameters defaultParameters(config)
        description("Сценарій: Спрощена процедура для потреб оборони.")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/simple_defense.txt"

         steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $openProcedure $defaultArgs $fast_auction $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell(shellRebot)
         }
    }

    job("${config.environment}_simple_defence_one_bid") {
        parameters defaultParameters(config)
        description("Сценарій: Спрощена процедура для потреб оборони (з одним учасником).")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/simple_defense_one_bid.txt"

         steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $openProcedure $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell(shellRebot)
         }
    }

//////////////////////////////////////////////////////////////////////////////////////////////////
//                 VAT TRUE/FALSE TESTING
//////////////////////////////////////////////////////////////////////////////////////////////////

    job("${config.environment}_aboveThresholdEU_vat_true_false") {
        parameters defaultParameters(config)
        description("Сценарій: Відкрита процедура (короткий сценарій) оголошена з ПДВ, контракт укладено без ПДВ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/openeu_simple.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $openProcedure $defaultArgs $no_auction $openeu_acceleration $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/openeu_simple.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $openProcedure $defaultArgs -v VAT_INCLUDED:False $no_auction $openeu_acceleration $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/openeu_simple.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $openProcedure $defaultArgs -v VAT_INCLUDED:False $no_auction $openeu_acceleration $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs -i modify_contract_vat_to_true -i modify_contract_amount_net -i modify_contract_value -i modify_contract_invalid_amountNet -i modify_contract_invalid_amount_tender_vat_false $params")
            shell("$robotWrapper $contractmanagement $defaultArgs -i change_contract_amountNet -i change_contract_amount -i change_amount_paid $params")
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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/openua_simple.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $openProcedure $defaultArgs $no_auction $openua_acceleration $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/openua_simple.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $openProcedure $defaultArgs $no_auction -v VAT_INCLUDED:False $openua_acceleration $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/openua_simple.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $openProcedure $defaultArgs -v VAT_INCLUDED:False $no_auction $openua_acceleration $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs -i modify_contract_vat_to_true -i modify_contract_amount_net -i modify_contract_value -i modify_contract_invalid_amountNet -i modify_contract_invalid_amount_tender_vat_false $params")
            shell("$robotWrapper $contractmanagement $defaultArgs -i change_contract_amountNet -i change_contract_amount -i change_amount_paid $params")
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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/below_simple.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan $params")
            shell("$robotWrapper $openProcedure $defaultArgs $no_auction $below_acceleration $params")
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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/below_simple.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan $params")
            shell("$robotWrapper $openProcedure $defaultArgs $no_auction -v VAT_INCLUDED:False $below_acceleration $params")
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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/below_simple.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan $params")
            shell("$robotWrapper $openProcedure $defaultArgs $no_auction -v VAT_INCLUDED:False $below_acceleration $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs -i modify_contract_vat_to_true -i modify_contract_amount_net -i modify_contract_value -i modify_contract_invalid_amountNet -i modify_contract_invalid_amount_tender_vat_false $params")
            shell("$robotWrapper $contractmanagement $defaultArgs -i change_contract_amountNet -i change_contract_amount -i change_amount_paid $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs $no_auction $competitive_dialogue_EU_acceleration $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs $no_auction -v VAT_INCLUDED:False $competitive_dialogue_EU_acceleration $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs $no_auction -v VAT_INCLUDED:False $competitive_dialogue_EU_acceleration $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs -i modify_contract_vat_to_true -i modify_contract_amount_net -i modify_contract_value -i modify_contract_invalid_amountNet -i modify_contract_invalid_amount_tender_vat_false $params")
            shell("$robotWrapper $contractmanagement $defaultArgs -i change_contract_amountNet -i change_contract_amount -i change_amount_paid $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs $no_auction $competitive_dialogue_UA_acceleration $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs $no_auction -v VAT_INCLUDED:False $competitive_dialogue_UA_acceleration $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs $no_auction -v VAT_INCLUDED:False $competitive_dialogue_UA_acceleration $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs -i modify_contract_vat_to_true -i modify_contract_amount_net -i modify_contract_value -i modify_contract_invalid_amountNet -i modify_contract_invalid_amount_tender_vat_false $params")
            shell("$robotWrapper $contractmanagement $defaultArgs -i change_contract_amountNet -i change_contract_amount -i change_amount_paid $params")
            shell(shellRebot)
        }
    }

//////////////////////////////////////////////////////////////////////////////////////////////////
//                 DASU TESTING
//////////////////////////////////////////////////////////////////////////////////////////////////

    ["cancelled", "closed", "completed", "complete_proceed_num", "declined", "active_stopped", "adressed_stopped",
    "declined_stopped"].each { String scenario ->
        job("${config.environment}_dasu_$scenario") {
            parameters defaultParameters(config)
            description("Сценарій: ДАСУ")
            keepDependencies(false)
            disabled(false)
            concurrentBuild(config.concurrentBuild)
            scm defaultScm
            publishers defaultPublishers
            wrappers defaultWrappers(true, 10800)
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

//////////////////////////////////////////////////////////////////////////////////////////////////
//                 FEED READING
//////////////////////////////////////////////////////////////////////////////////////////////////

    job("${config.environment}_feed_reading") {
        parameters defaultParameters(config)
        description("Сценарій: Вичітування довільного набіру даних с ЦБД")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
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

//////////////////////////////////////////////////////////////////////////////////////////////////
//                 CPB PLANS AGGREGATION TESTING
//////////////////////////////////////////////////////////////////////////////////////////////////

    job("${config.environment}_aboveThresholdUA_plans_aggregation") {
        parameters defaultParameters(config)
        description("Сценарій: Надпорогова закупівля.")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/plans_aggregation.txt"
        String accelerate_aboveThresholdUA = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdUA\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $openProcedure $defaultArgs $no_auction $accelerate_aboveThresholdUA $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell("$robotWrapper $contractmanagement $defaultArgs $params")
            shell(shellRebot)
        }
    }

//////////////////////////////////////////////////////////////////////////////////////////////////
//                  PLANNING
//////////////////////////////////////////////////////////////////////////////////////////////////

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

//////////////////////////////////////////////////////////////////////////////////////////////////
//                  COST/GMDN TESTING
//////////////////////////////////////////////////////////////////////////////////////////////////

    job("${config.environment}_below_cost") {
        parameters defaultParameters(config)
        description("Сценарій: Допорогова закупівля з індексом UA-ROAD")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/below_simple.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v ROAD_INDEX:True $params")
            shell("$robotWrapper $openProcedure $defaultArgs $no_auction $below_acceleration $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs -i modify_contract_amount_net -i modify_contract_value $params")
            shell("$robotWrapper $contractmanagement $defaultArgs -i change_contract_amountNet -i change_contract_amount -i change_amount_paid $params")
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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/below_simple.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v GMDN_INDEX:True $params")
            shell("$robotWrapper $openProcedure $defaultArgs $no_auction $below_acceleration $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs -i modify_contract_amount_net -i modify_contract_value $params")
            shell("$robotWrapper $contractmanagement $defaultArgs -i change_contract_amountNet -i change_contract_amount -i change_amount_paid $params")
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
        wrappers defaultWrappers(true, 10800)
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

    job("${config.environment}_belowThreshold_moz_1") {
        parameters defaultParameters(config)
        description("Сценарій: Допорогова закупівля фармацевтичної продукції")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/mnn_1.txt"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MOZ_INTEGRATION:True $params")
            shell("$robotWrapper $openProcedure $defaultArgs $no_auction $below_acceleration $params")
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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/mnn_2.txt"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MOZ_INTEGRATION:True $params")
            shell("$robotWrapper $openProcedure $defaultArgs $no_auction $below_acceleration $params")
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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/mnn_3.txt"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MOZ_INTEGRATION:True $params")
            shell("$robotWrapper $openProcedure $defaultArgs $no_auction $below_acceleration $params")
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
        wrappers defaultWrappers(true, 10800)
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

//////////////////////////////////////////////////////////////////////////////////////////////////
//                  EDR/DFS TESTING
//////////////////////////////////////////////////////////////////////////////////////////////////

    job("${config.environment}_aboveThresholdEU_EDR_DFS") {
        parameters defaultParameters(config)
        description("Сценарій: Надпорогова закупівля з публікацією англійською мовою.")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure

        String defaultArgs = "-A robot_tests_arguments/openeu_edr_dfs.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,7.5],\"accelerator\":5760}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $openProcedure $defaultArgs $no_auction $accelerate_openeu $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_aboveThresholdUA_EDR_DFS") {
        parameters defaultParameters(config)
        description("Сценарій: Надпорогова закупівля.")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure

        String defaultArgs = "-A robot_tests_arguments/openua_edr_dfs.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdUA\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $openProcedure $defaultArgs $no_auction $accelerate_openua $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_competitiveDialogueEU_EDR_DFS") {
        parameters defaultParameters(config)
        description("Сценарій: Конкурентний діалог з публікацією англійською мовою")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure

        String defaultArgs = "-A robot_tests_arguments/competitive_dialogue_edr_dfs.txt"
        String accelerate_open_competitive_dialogue = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,15],\"accelerator\":2880}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $openProcedure $defaultArgs $no_auction $accelerate_open_competitive_dialogue $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_competitiveDialogueUA_EDR_DFS") {
        parameters defaultParameters(config)
        description("Сценарій: Конкурентний діалог для надпорогових закупівель українською мовою")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure

        String defaultArgs = "-A robot_tests_arguments/competitive_dialogue_UA_edr_dfs.txt"
        String accelerate_open_competitive_dialogue = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,15],\"accelerator\":2880}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $openProcedure $defaultArgs $no_auction $accelerate_open_competitive_dialogue $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_esco_EDR_DFS") {
        parameters defaultParameters(config)
        description("Сценарій: Відкриті торги для закупівлі енергосервісу")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure

        String defaultArgs = "-A robot_tests_arguments/esco_edr_dfs.txt"
        String accelerate_open_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"enquiry\":[0,1],\"tender\":[1,7.5],\"accelerator\":5760}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $openProcedure $defaultArgs $no_auction $accelerate_open_esco $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell(shellRebot)
        }
    }

//////////////////////////////////////////////////////////////////////////////////////////////////
//                   FULL COMPLAINTS TESTING (ABOVETHRESHOLD_EU)
//////////////////////////////////////////////////////////////////////////////////////////////////

    job("${config.environment}_openeu_complaint_tender_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, прийнята та задоволена АМКУ та Учасником виконано рішення АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_resolved.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_mistaken.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_declined.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_stopped.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_invalid.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints  $defaultArgs $accelerate_openeu $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_openeu_complaint_award_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на визначення переможця, прийнята та задоволена АМКУ та Учасником виконано рішення АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_award_resolved.txt"
        String openeu_pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_qualifications -i pre-qualification_view"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs $openeu_pre_qualification $no_auction $accelerate_openeu $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_openeu_complaint_award_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на визначення переможця, позначена Учасником як помилково створена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

         String defaultArgs = "-A robot_tests_arguments/complaint_award_mistaken.txt"
         String openeu_pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_qualifications -i pre-qualification_view"
         String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs $openeu_pre_qualification $no_auction $accelerate_openeu $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_openeu_complaint_award_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на визначення переможця, прийнята до розгляду та відхилена АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_award_declined.txt"
        String openeu_pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_qualifications -i pre-qualification_view"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs $openeu_pre_qualification $no_auction $accelerate_openeu $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_openeu_complaint_award_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на визначення переможця, прийнята, розгляду зупинено АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_award_stopped.txt"
        String openeu_pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_qualifications -i pre-qualification_view"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs $openeu_pre_qualification $no_auction $accelerate_openeu $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_openeu_complaint_award_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на визначення переможця, АМКУ залишив скаргу без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_award_invalid.txt"
        String openeu_pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_qualifications -i pre-qualification_view"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs $openeu_pre_qualification $no_auction $accelerate_openeu $params")
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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_qualification_resolved.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_qualification_mistaken.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_qualification_invalid.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_qualification_declined.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_qualification_stopped.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_resolved.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_mistaken.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_invalid.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_declined.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_stopped.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_resolved.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_mistaken.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_invalid.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_declined.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_stopped.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_resolved.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_mistaken.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_invalid.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_declined.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_stopped.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $complaints $defaultArgs $accelerate_openeu $params")
            shell(shellRebot)
        }
    }

//////////////////////////////////////////////////////////////////////////////////////////////////
//                   FULL COMPLAINTS TESTING (SIMPLE DEFENSE)
//////////////////////////////////////////////////////////////////////////////////////////////////

    job("${config.environment}_complaint_simple_defence_first_award_cancel") {
        parameters defaultParameters(config)
        description("Сценарій: Неможливо подати скаргу після першого скасування рішення")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/defense_complaint_award_cancel_pending.txt"
        String accelerate_simple_defense = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"simple.defense\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"

         steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $complaints $defaultArgs $no_auction $accelerate_simple_defense $params")
            shell(shellRebot)
         }
    }

    job("${config.environment}_complaint_simple_defence_first_award_disqualification") {
        parameters defaultParameters(config)
        description("Сценарій: Неможливо подати скаргу після дискваліфікації першого учасника")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/defense_complaint_award_cancel_unsuc_pending.txt"
        String accelerate_simple_defense = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"simple.defense\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"

         steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $complaints $defaultArgs $no_auction $accelerate_simple_defense $params")
            shell(shellRebot)
         }
    }

    job("${config.environment}_complaint_simple_defence_second_award_cancel") {
        parameters defaultParameters(config)
        description("Сценарій: Неможливо подати скаргу після другого скасування рішення")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/defense_complaint_award_cancel_unsuc_cancel_pending.txt"
        String accelerate_simple_defense = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"simple.defense\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"

         steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $complaints $defaultArgs $no_auction $accelerate_simple_defense $params")
            shell(shellRebot)
         }
    }

    job("${config.environment}_complaint_simple_defence_second_award_disqualification") {
        parameters defaultParameters(config)
        description("Сценарій: Неможливо подати скаргу після дискваліфікації другого учасника")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/defense_complaint_award_cancel_unsuc_cancel_unsuc_pending.txt"
        String accelerate_simple_defense = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"simple.defense\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"

         steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $complaints $defaultArgs $no_auction $accelerate_simple_defense $params")
            shell(shellRebot)
         }
    }

    job("${config.environment}_complaint_simple_defence_third_award_cancel") {
        parameters defaultParameters(config)
        description("Сценарій: Неможливо подати скаргу після третього скасування рішення")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/defense_complaint_award_cancel_unsuc_cancel_unsuc_cancel_pending.txt"
        String accelerate_simple_defense = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"simple.defense\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"

         steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $complaints $defaultArgs $no_auction $accelerate_simple_defense $params")
            shell(shellRebot)
         }
    }

    job("${config.environment}_complaint_simple_defence_third_award_disqualification") {
        parameters defaultParameters(config)
        description("Сценарій: Неможливо подати скаргу після дискваліфікації третього учасника")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/defense_complaint_award_cancel_unsuc_cancel_unsuc_cancel_unsuc.txt"
        String accelerate_simple_defense = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"simple.defense\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"

         steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $complaints $defaultArgs $no_auction $accelerate_simple_defense $params")
            shell(shellRebot)
         }
    }

    job("complaint_simple_defense_award_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на визначення переможця, прийнята та задоволена АМКУ та Учасником виконано рішення АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_award_resolved.txt"
        String accelerate_simple_defense = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"simple.defense\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"
        String mode = "-v MODE:simple.defense"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $complaints  $defaultArgs $mode $no_auction $accelerate_simple_defense $params")
            shell(shellRebot)
        }
    }

    job("complaint_simple_defense_award_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на визначення переможця, позначена Учасником як помилково створена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

         String defaultArgs = "-A robot_tests_arguments/complaint_award_mistaken.txt"
         String accelerate_simple_defense = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"simple.defense\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"
         String mode = "-v MODE:simple.defense"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $complaints  $defaultArgs $mode $no_auction $accelerate_simple_defense $params")
            shell(shellRebot)
        }
    }

    job("complaint_simple_defense_award_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на визначення переможця, прийнята до розгляду та відхилена АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_award_declined.txt"
        String accelerate_simple_defense = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"simple.defense\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"
        String mode = "-v MODE:simple.defense"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $complaints  $defaultArgs $mode $no_auction $accelerate_simple_defense $params")
            shell(shellRebot)
        }
    }

    job("complaint_simple_defense_award_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на визначення переможця, прийнята, розгляду зупинено АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_award_stopped.txt"
        String accelerate_simple_defense = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"simple.defense\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"
        String mode = "-v MODE:simple.defense"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $complaints  $defaultArgs $mode $no_auction $accelerate_simple_defense $params")
            shell(shellRebot)
        }
    }

    job("complaint_simple_defense_award_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на визначення переможця, АМКУ залишив скаргу без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_award_invalid.txt"
        String accelerate_simple_defense = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"simple.defense\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"
        String mode = "-v MODE:simple.defense"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $no_auction $accelerate_simple_defense $params")
            shell(shellRebot)
        }
    }

    job("complaint_simple_defense_cancel_lot_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота задоволена та виконана Замовником")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_resolved.txt"
        String accelerate_simple_defense = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"simple.defense\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"
        String mode = "-v MODE:simple.defense"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_simple_defense $params")
            shell(shellRebot)
        }
    }

    job("complaint_simple_defense_cancel_lot_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота створена помилково")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_mistaken.txt"
        String accelerate_simple_defense = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"simple.defense\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"
        String mode = "-v MODE:simple.defense"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_simple_defense $params")
            shell(shellRebot)
        }
    }

    job("complaint_simple_defense_cancel_lot_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота залишена без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_invalid.txt"
        String accelerate_simple_defense= "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"simple.defense\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"
        String mode = "-v MODE:simple.defense"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_simple_defense $params")
            shell(shellRebot)
        }
    }

    job("complaint_simple_defense_cancel_lot_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота відхилена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_declined.txt"
        String accelerate_simple_defense = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"simple.defense\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"
        String mode = "-v MODE:simple.defense"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_simple_defense $params")
            shell(shellRebot)
        }
    }

    job("complaint_simple_defense_cancel_lot_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота розгляд зупинено")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_stopped.txt"
        String accelerate_simple_defense = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"simple.defense\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"
        String mode = "-v MODE:simple.defense"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_simple_defense $params")
            shell(shellRebot)
        }
    }

    job("complaint_simple_defense_cancel_tender_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера задоволена та виконана Замовником")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_resolved.txt"
        String accelerate_simple_defense = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"simple.defense\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"
        String mode = "-v MODE:simple.defense"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_simple_defense $params")
            shell(shellRebot)
        }
    }

    job("complaint_simple_defense_cancel_tender_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера створена помилково")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_mistaken.txt"
        String accelerate_simple_defense = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"simple.defense\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"
        String mode = "-v MODE:simple.defense"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_simple_defense $params")
            shell(shellRebot)
        }
    }

    job("complaint_simple_defense_cancel_tender_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера залишена без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_invalid.txt"
        String accelerate_simple_defense = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"simple.defense\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"
        String mode = "-v MODE:simple.defense"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_simple_defense $params")
            shell(shellRebot)
        }
    }

    job("complaint_simple_defense_cancel_tender_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера відхилена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_declined.txt"
        String accelerate_simple_defense = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"simple.defense\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"
        String mode = "-v MODE:simple.defense"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_simple_defense $params")
            shell(shellRebot)
        }
    }

    job("complaint_simple_defense_cancel_tender_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера розгляд зупинено")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_stopped.txt"
        String accelerate_simple_defense = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"simple.defense\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"
        String mode = "-v MODE:simple.defense"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_simple_defense $params")
            shell(shellRebot)
        }
    }

    job("complaint_simple_defense_lot_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота виконана Замовником")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_resolved.txt"
        String accelerate_simple_defense = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"simple.defense\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"
        String mode = "-v MODE:simple.defense"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_simple_defense $params")
            shell(shellRebot)
        }
    }

    job("complaint_simple_defense_lot_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота створена помилково")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_mistaken.txt"
        String accelerate_simple_defense = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"simple.defense\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"
        String mode = "-v MODE:simple.defense"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_simple_defense $params")
            shell(shellRebot)
        }
    }

    job("complaint_simple_defense_lot_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота залишена без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_invalid.txt"
        String accelerate_simple_defense = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"simple.defense\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"
        String mode = "-v MODE:simple.defense"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_simple_defense $params")
            shell(shellRebot)
        }
    }

    job("complaint_simple_defense_lot_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота відхилена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_declined.txt"
        String accelerate_simple_defense = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"simple.defense\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"
        String mode = "-v MODE:simple.defense"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_simple_defense $params")
            shell(shellRebot)
        }
    }

    job("complaint_simple_defense_lot_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота розгляд зупинено")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_stopped.txt"
        String accelerate_simple_defense = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"simple.defense\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"
        String mode = "-v MODE:simple.defense"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_simple_defense $params")
            shell(shellRebot)
        }
    }

    job("complaint_simple_defense_tender_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, прийнята та задоволена АМКУ та Учасником виконано рішення АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_resolved.txt"
        String accelerate_simple_defense = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"simple.defense\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:simple.defense"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_simple_defense $params")
            shell(shellRebot)
        }
    }

    job("complaint_simple_defense_tender_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, позначена Учасником як помилково створена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_mistaken.txt"
        String accelerate_simple_defense = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"simple.defense\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:simple.defense"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_simple_defense $params")
            shell(shellRebot)
        }
    }

    job("complaint_simple_defense_tender_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, прийнята до розгляду та відхилена АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_declined.txt"
        String accelerate_simple_defense = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"simple.defense\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:simple.defense"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_simple_defense $params")
            shell(shellRebot)
        }
    }

    job("complaint_simple_defense_tender_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, прийнята, розгляду зупинено АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_stopped.txt"
        String accelerate_simple_defense = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"simple.defense\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:simple.defense"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $complaints  $defaultArgs $mode $accelerate_simple_defense $params")
            shell(shellRebot)
        }
    }

    job("complaint_simple_defense_tender_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, АМКУ залишив скаргу без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_invalid.txt"
        String accelerate_simple_defense = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"simple.defense\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:simple.defense"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $complaints  $defaultArgs $mode $accelerate_simple_defense $params")
            shell(shellRebot)
        }
    }

//////////////////////////////////////////////////////////////////////////////////////////////////
//                  ALP/24 HOURS
//////////////////////////////////////////////////////////////////////////////////////////////////

    job("${config.environment}_aboveThresholdUA_24_hours_award") {
        parameters defaultParameters(config)
        description("Сценарій: Повідомлення про невідповідність пропозиції на єтапі кваліфікації")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/24_hours_qual.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openeu\":{\"tender\":[1,10],\"accelerator\":4320}}}}'"

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
        wrappers defaultWrappers(true, 10800)
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

//////////////////////////////////////////////////////////////////////////////////////////////////
//                  CANCELLATION
//////////////////////////////////////////////////////////////////////////////////////////////////

    job("cancellation_tendering_aboveThresholdEU") {
        parameters defaultParameters(config)
        description("Сценарій: Скасування надпорогової закупівлі з публікацією англійською мовою. Етап active.tendering")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"openua\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $cancellation $defaultArgs -v MODE:aboveThresholdUA $no_auction $accelerate_openua $params")
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation.txt"
        String accelerate_open_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"enquiry\":[0,1],\"tender\":[1,5],\"accelerator\":14400}}}}'"

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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_qualification_esco.txt"
        String accelerate_open_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"enquiry\":[0,1],\"tender\":[1,7.5],\"accelerator\":5760}}}}'"

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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_awarded_esco.txt"
        String accelerate_open_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"enquiry\":[0,1],\"tender\":[1,7.5],\"accelerator\":5760}}}}'"

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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/cancellation_pre_qualification_esco.txt"
        String accelerate_open_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"enquiry\":[0,1],\"tender\":[1,7.5],\"accelerator\":5760}}}}'"

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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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
        wrappers defaultWrappers(true, 10800)
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

//////////////////////////////////////////////////////////////////////////////////////////////////
//                  PRICE QUOTATION
//////////////////////////////////////////////////////////////////////////////////////////////////

    job("${config.environment}_priceQuotation_full") {
        parameters defaultParameters(config)
        description("Сценарій: Процедура PQ від початку до кінця")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/priceQuotation.txt"

        steps {
            shell(shellBuildout)
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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/priceQuotation_cancellation_after_active_tendering.txt"

        steps {
            shell(shellBuildout)
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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/priceQuotation_cancellation_after_active_qualification.txt"

        steps {
            shell(shellBuildout)
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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/priceQuotation_cancellation_after_active_award.txt"

        steps {
            shell(shellBuildout)
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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/priceQuotation_cancelled_without_bids.txt"

        steps {
            shell(shellBuildout)
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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/priceQuotation_unsuccessful.txt"

        steps {
            shell(shellBuildout)
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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/priceQuotation_bot_unsuccessful_shortlistedfirms_empty.txt"

        steps {
            shell(shellBuildout)
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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/priceQuotation_bot_unsuccessful_hidden_profile.txt"

        steps {
            shell(shellBuildout)
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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/priceQuotation_bot_unsuccessful_unknown_profile.txt"

        steps {
            shell(shellBuildout)
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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/priceQuotation_bot_unsuccessful.txt"

        steps {
            shell(shellBuildout)
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
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/priceQuotation_negative.txt"

        steps {
            shell(shellBuildout)
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
            shell("$robotWrapper $planning -i create_plan -i find_plan -v mode:priceQuotation $params")
            shell("$robotWrapper $priceQuotation $defaultArgs $params")
            shell(shellRebot)
        }
    }

//////////////////////////////////////////////////////////////////////////////////////////////////
//                  CRITERIA TESTING
//////////////////////////////////////////////////////////////////////////////////////////////////

    job("${config.environment}_criteria_patch_evidence") {
        parameters defaultParameters(config)
        description("Сценарій: Зміна evidence критерія")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/criteria_patch_evidence.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdUA\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"

         steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $openProcedure $defaultArgs $no_auction $accelerate_openua $params")
            shell(shellRebot)
         }
    }

//////////////////////////////////////////////////////////////////////////////////////////////////
//                  LLC TESTING
//////////////////////////////////////////////////////////////////////////////////////////////////

    job("${config.environment}_aboveThresholdEU_LLC") {
        parameters defaultParameters(config)
        description("Сценарій: Надпорогова закупівля з публікацією англійською мовою. Життєвий цикл")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables {defaultEnv() defaultEnv_dfs()}

        String defaultArgs = "-A robot_tests_arguments/openeu_llc.txt"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $openProcedure $defaultArgs $fast_auction $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell("$robotWrapper $contractmanagement $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_aboveThresholdUA_LLC") {
        parameters defaultParameters(config)
        description("Сценарій: Надпорогова закупівля. Життєвий цикл")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/openua_llc.txt"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $openProcedure $defaultArgs $fast_auction $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell("$robotWrapper $contractmanagement $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_simple_defence_LLC") {
        parameters defaultParameters(config)
        description("Сценарій: Спрощена процедура для потреб оборони. Життєвий цикл")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/simple_defense_llc.txt"

         steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $openProcedure $defaultArgs $fast_auction $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell(shellRebot)
         }
    }

//////////////////////////////////////////////////////////////////////////////////////////////////
//                  FULL COMPLAINTS TESTING (ABOVETHRESHOLD_UA)
//////////////////////////////////////////////////////////////////////////////////////////////////

    job("complaint_aboveThresholdUA_award_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на визначення переможця, прийнята та задоволена АМКУ та Учасником виконано рішення АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_award_resolved.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdUA\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"
        String mode = "v MODE:aboveThresholdUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $complaints  $defaultArgs $mode $no_auction $accelerate_openua $params")
            shell(shellRebot)
        }
    }

    job("complaint_aboveThresholdUA_award_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на визначення переможця, позначена Учасником як помилково створена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

         String defaultArgs = "-A robot_tests_arguments/complaint_award_mistaken.txt"
         String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdUA\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"
         String mode = "v MODE:aboveThresholdUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $complaints  $defaultArgs $mode $no_auction $accelerate_openua $params")
            shell(shellRebot)
        }
    }

    job("complaint_aboveThresholdUA_award_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на визначення переможця, прийнята до розгляду та відхилена АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_award_declined.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdUA\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"
        String mode = "v MODE:aboveThresholdUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $complaints  $defaultArgs $mode $no_auction $accelerate_openua $params")
            shell(shellRebot)
        }
    }

    job("complaint_aboveThresholdUA_award_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на визначення переможця, прийнята, розгляду зупинено АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_award_stopped.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdUA\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"
        String mode = "v MODE:aboveThresholdUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $complaints  $defaultArgs $mode $no_auction $accelerate_openua $params")
            shell(shellRebot)
        }
    }

    job("complaint_aboveThresholdUA_award_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на визначення переможця, АМКУ залишив скаргу без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_award_invalid.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdUA\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"
        String mode = "v MODE:aboveThresholdUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $no_auction $accelerate_openua $params")
            shell(shellRebot)
        }
    }

    job("complaint_aboveThresholdUA_cancel_lot_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота задоволена та виконана Замовником")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_resolved.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdUA\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $complaints $defaultArgs -v MODE:aboveThresholdUA $accelerate_openua $params")
            shell(shellRebot)
        }
    }

    job("complaint_aboveThresholdUA_cancel_lot_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота створена помилково")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_mistaken.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdUA\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $complaints $defaultArgs -v MODE:aboveThresholdUA $accelerate_openua $params")
            shell(shellRebot)
        }
    }

    job("complaint_aboveThresholdUA_cancel_lot_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота залишена без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_invalid.txt"
        String accelerate_openua= "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdUA\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $complaints $defaultArgs -v MODE:aboveThresholdUA $accelerate_openua $params")
            shell(shellRebot)
        }
    }

    job("complaint_aboveThresholdUA_cancel_lot_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота відхилена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_declined.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdUA\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $complaints $defaultArgs -v MODE:aboveThresholdUA $accelerate_openua $params")
            shell(shellRebot)
        }
    }

    job("complaint_aboveThresholdUA_cancel_lot_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота розгляд зупинено")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_stopped.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdUA\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $complaints $defaultArgs -v MODE:aboveThresholdUA $accelerate_openua $params")
            shell(shellRebot)
        }
    }

    job("complaint_aboveThresholdUA_cancel_tender_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера задоволена та виконана Замовником")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_resolved.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdUA\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"
        String mode = "-v MODE:aboveThresholdUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_openua $params")
            shell(shellRebot)
        }
    }

    job("complaint_aboveThresholdUA_cancel_tender_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера створена помилково")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_mistaken.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdUA\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $complaints $defaultArgs -v MODE:aboveThresholdUA $accelerate_openua $params")
            shell(shellRebot)
        }
    }

    job("complaint_aboveThresholdUA_cancel_tender_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера залишена без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_invalid.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdUA\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"
        String mode = "-v MODE:aboveThresholdUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_openua $params")
            shell(shellRebot)
        }
    }

    job("complaint_aboveThresholdUA_cancel_tender_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера відхилена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_declined.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdUA\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $complaints $defaultArgs -v MODE:aboveThresholdUA $accelerate_openua $params")
            shell(shellRebot)
        }
    }

    job("complaint_aboveThresholdUA_cancel_tender_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера розгляд зупинено")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_stopped.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdUA\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $complaints $defaultArgs -v MODE:aboveThresholdUA $accelerate_openua $params")
            shell(shellRebot)
        }
    }

    job("complaint_aboveThresholdUA_lot_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота виконана Замовником")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_resolved.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdUA\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $complaints $defaultArgs -v MODE:aboveThresholdUA $accelerate_openua $params")
            shell(shellRebot)
        }
    }

    job("complaint_aboveThresholdUA_lot_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота створена помилково")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_mistaken.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdUA\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $complaints $defaultArgs -v MODE:aboveThresholdUA $accelerate_openua $params")
            shell(shellRebot)
        }
    }

    job("complaint_aboveThresholdUA_lot_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота залишена без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_invalid.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdUA\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $complaints $defaultArgs -v MODE:aboveThresholdUA $accelerate_openua $params")
            shell(shellRebot)
        }
    }

    job("complaint_aboveThresholdUA_lot_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота відхилена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_declined.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdUA\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $complaints $defaultArgs -v MODE:aboveThresholdUA $accelerate_openua $params")
            shell(shellRebot)
        }
    }

    job("complaint_aboveThresholdUA_lot_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота розгляд зупинено")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_stopped.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdUA\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $complaints $defaultArgs -v MODE:aboveThresholdUA $accelerate_openua $params")
            shell(shellRebot)
        }
    }

    job("complaint_aboveThresholdUA_tender_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, прийнята та задоволена АМКУ та Учасником виконано рішення АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_resolved.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:aboveThresholdUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_openua $params")
            shell(shellRebot)
        }
    }

    job("complaint_aboveThresholdUA_tender_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, позначена Учасником як помилково створена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_mistaken.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:aboveThresholdUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_openua $params")
            shell(shellRebot)
        }
    }

    job("complaint_aboveThresholdUA_tender_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, прийнята до розгляду та відхилена АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_declined.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:aboveThresholdUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_openua $params")
            shell(shellRebot)
        }
    }

    job("complaint_aboveThresholdUA_tender_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, прийнята, розгляду зупинено АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_stopped.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:aboveThresholdUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $complaints  $defaultArgs $mode $accelerate_openua $params")
            shell(shellRebot)
        }
    }

    job("complaint_aboveThresholdUA_tender_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, АМКУ залишив скаргу без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_invalid.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:aboveThresholdUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $complaints  $defaultArgs $mode $accelerate_openua $params")
            shell(shellRebot)
        }
    }

//////////////////////////////////////////////////////////////////////////////////////////////////
//                  FULL COMPLAINTS TESTING (ESCO)
//////////////////////////////////////////////////////////////////////////////////////////////////

    job("complaint_esco_tender_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, прийнята та задоволена АМКУ та Учасником виконано рішення АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_resolved.txt"
        String accelerate_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:esco"
        String funding_kind = "-v FUNDING_KIND:budget"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $funding_kind $accelerate_esco $params")
            shell(shellRebot)
        }
    }

    job("complaint_esco_tender_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, позначена Учасником як помилково створена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_mistaken.txt"
        String accelerate_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:esco"
        String funding_kind = "-v FUNDING_KIND:budget"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $funding_kind $accelerate_esco $params")
            shell(shellRebot)
        }
    }

    job("complaint_esco_tender_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, прийнята до розгляду та відхилена АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_declined.txt"
        String accelerate_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:esco"
        String funding_kind = "-v FUNDING_KIND:budget"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $funding_kind $accelerate_esco $params")
            shell(shellRebot)
        }
    }

    job("complaint_esco_tender_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, прийнята, розгляду зупинено АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_stopped.txt"
        String accelerate_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:esco"
        String funding_kind = "-v FUNDING_KIND:budget"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $complaints  $defaultArgs $mode $funding_kind $accelerate_esco $params")
            shell(shellRebot)
        }
    }

    job("complaint_esco_tender_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, АМКУ залишив скаргу без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_invalid.txt"
        String accelerate_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:esco"
        String funding_kind = "-v FUNDING_KIND:budget"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $complaints  $defaultArgs $mode $funding_kind $accelerate_esco $params")
            shell(shellRebot)
        }
    }

    job("complaint_esco_award_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на визначення переможця, прийнята та задоволена АМКУ та Учасником виконано рішення АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_award_resolved.txt"
        String esco_pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_qualifications -i pre-qualification_view"
        String accelerate_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:esco"
        String funding_kind = "-v FUNDING_KIND:budget"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $complaints $defaultArgs $esco_pre_qualification $mode $funding_kind $no_auction $accelerate_esco $params")
            shell(shellRebot)
        }
    }

    job("complaint_esco_award_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на визначення переможця, позначена Учасником як помилково створена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

         String defaultArgs = "-A robot_tests_arguments/complaint_award_mistaken.txt"
         String esco_pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_qualifications -i pre-qualification_view"
         String accelerate_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
         String mode = "-v MODE:esco"
         String funding_kind = "-v FUNDING_KIND:budget"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $complaints $defaultArgs $esco_pre_qualification $mode $funding_kind $no_auction $accelerate_esco $params")
            shell(shellRebot)
        }
    }

    job("complaint_esco_award_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на визначення переможця, прийнята до розгляду та відхилена АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_award_declined.txt"
        String esco_pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_qualifications -i pre-qualification_view"
        String accelerate_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:esco"
        String funding_kind = "-v FUNDING_KIND:budget"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $complaints $defaultArgs $esco_pre_qualification $mode $funding_kind $no_auction $accelerate_esco $params")
            shell(shellRebot)
        }
    }

    job("complaint_esco_award_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на визначення переможця, прийнята, розгляду зупинено АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_award_stopped.txt"
        String esco_pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_qualifications -i pre-qualification_view"
        String accelerate_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:esco"
        String funding_kind = "-v FUNDING_KIND:budget"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $complaints $defaultArgs $esco_pre_qualification $mode $funding_kind $no_auction $accelerate_esco $params")
            shell(shellRebot)
        }
    }

    job("complaint_esco_award_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на визначення переможця, АМКУ залишив скаргу без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_award_invalid.txt"
        String esco_pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_qualifications -i pre-qualification_view"
        String accelerate_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:esco"
        String funding_kind = "-v FUNDING_KIND:budget"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $complaints $defaultArgs $esco_pre_qualification $mode $funding_kind $no_auction $accelerate_esco $params")
            shell(shellRebot)
        }
    }

    job("complaint_esco_qualification_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на пре-кваліфікацію учасника задоволена Замовником")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_qualification_resolved.txt"
        String accelerate_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:esco"
        String funding_kind = "-v FUNDING_KIND:budget"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $funding_kind $accelerate_esco $params")
            shell(shellRebot)
        }
    }

    job("complaint_esco_qualification_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на пре-кваліфікацію учасника створена помилково")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_qualification_mistaken.txt"
        String accelerate_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:esco"
        String funding_kind = "-v FUNDING_KIND:budget"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $funding_kind $accelerate_esco $params")
            shell(shellRebot)
        }
    }

    job("complaint_esco_qualification_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на пре-кваліфікацію учасника залишена без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_qualification_invalid.txt"
        String accelerate_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:esco"
        String funding_kind = "-v FUNDING_KIND:budget"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $funding_kind $accelerate_esco $params")
            shell(shellRebot)
        }
    }

    job("complaint_esco_qualification_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на пре-кваліфікацію учасника відхилена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_qualification_declined.txt"
        String accelerate_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:esco"
        String funding_kind = "-v FUNDING_KIND:budget"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $funding_kind $accelerate_esco $params")
            shell(shellRebot)
        }
    }

    job("complaint_esco_qualification_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на пре-кваліфікацію учасника розгляд зупинено")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_qualification_stopped.txt"
        String accelerate_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:esco"
        String funding_kind = "-v FUNDING_KIND:budget"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $funding_kind $accelerate_esco $params")
            shell(shellRebot)
        }
    }

    job("complaint_esco_lot_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота виконана Замовником")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_resolved.txt"
        String accelerate_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:esco"
        String funding_kind = "-v FUNDING_KIND:budget"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $funding_kind $accelerate_esco $params")
            shell(shellRebot)
        }
    }

    job("complaint_esco_lot_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота створена помилково")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_mistaken.txt"
        String accelerate_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:esco"
        String funding_kind = "-v FUNDING_KIND:budget"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $funding_kind $accelerate_esco $params")
            shell(shellRebot)
        }
    }

    job("complaint_esco_lot_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота залишена без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_invalid.txt"
        String accelerate_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:esco"
        String funding_kind = "-v FUNDING_KIND:budget"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $funding_kind $accelerate_esco $params")
            shell(shellRebot)
        }
    }

    job("complaint_esco_lot_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота відхилена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_declined.txt"
        String accelerate_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:esco"
        String funding_kind = "-v FUNDING_KIND:budget"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $funding_kind $accelerate_esco $params")
            shell(shellRebot)
        }
    }

    job("complaint_esco_lot_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота розгляд зупинено")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_stopped.txt"
        String accelerate_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:esco"
        String funding_kind = "-v FUNDING_KIND:budget"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $funding_kind $accelerate_esco $params")
            shell(shellRebot)
        }
    }

    job("complaint_esco_cancel_tender_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера задоволена та виконана Замовником")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_resolved.txt"
        String accelerate_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:esco"
        String funding_kind = "-v FUNDING_KIND:budget"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $funding_kind $accelerate_esco $params")
            shell(shellRebot)
        }
    }

    job("complaint_esco_cancel_tender_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера створена помилково")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_mistaken.txt"
        String accelerate_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:esco"
        String funding_kind = "-v FUNDING_KIND:budget"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $funding_kind $accelerate_esco $params")
            shell(shellRebot)
        }
    }

    job("complaint_esco_cancel_tender_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера залишена без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_invalid.txt"
        String accelerate_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:esco"
        String funding_kind = "-v FUNDING_KIND:budget"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $funding_kind $accelerate_esco $params")
            shell(shellRebot)
        }
    }

    job("complaint_esco_cancel_tender_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера відхилена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_declined.txt"
        String accelerate_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:esco"
        String funding_kind = "-v FUNDING_KIND:budget"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $funding_kind $accelerate_esco $params")
            shell(shellRebot)
        }
    }

    job("complaint_esco_cancel_tender_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера розгляд зупинено")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_stopped.txt"
        String accelerate_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:esco"
        String funding_kind = "-v FUNDING_KIND:budget"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $funding_kind $accelerate_esco $params")
            shell(shellRebot)
        }
    }

    job("complaint_esco_cancel_lot_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота задоволена та виконана Замовником")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_resolved.txt"
        String accelerate_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:esco"
        String funding_kind = "-v FUNDING_KIND:budget"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $funding_kind $accelerate_esco $params")
            shell(shellRebot)
        }
    }

    job("complaint_esco_cancel_lot_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота створена помилково")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_mistaken.txt"
        String accelerate_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:esco"
        String funding_kind = "-v FUNDING_KIND:budget"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $funding_kind $accelerate_esco $params")
            shell(shellRebot)
        }
    }

    job("complaint_esco_cancel_lot_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота залишена без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_invalid.txt"
        String accelerate_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:esco"
        String funding_kind = "-v FUNDING_KIND:budget"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $funding_kind $accelerate_esco $params")
            shell(shellRebot)
        }
    }

    job("complaint_esco_cancel_lot_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота відхилена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_declined.txt"
        String accelerate_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:esco"
        String funding_kind = "-v FUNDING_KIND:budget"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $funding_kind $accelerate_esco $params")
            shell(shellRebot)
        }
    }

    job("complaint_esco_cancel_lot_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота розгляд зупинено")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_stopped.txt"
        String accelerate_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:esco"
        String funding_kind = "-v FUNDING_KIND:budget"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $funding_kind $accelerate_esco $params")
            shell(shellRebot)
        }
    }

//////////////////////////////////////////////////////////////////////////////////////////////////
//                  FULL COMPLAINTS TESTING (COMPETITIVE_DIALOGUE_EU)
//////////////////////////////////////////////////////////////////////////////////////////////////

    job("complaint_competitive_dialogue_eu_tender_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, прийнята та задоволена АМКУ та Учасником виконано рішення АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_resolved.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_tender_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, позначена Учасником як помилково створена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_mistaken.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_tender_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, прийнята до розгляду та відхилена АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_declined.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_tender_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, прийнята, розгляду зупинено АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_stopped.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints  $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_tender_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, АМКУ залишив скаргу без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_invalid.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints  $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_qualification_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на пре-кваліфікацію учасника задоволена Замовником")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_qualification_resolved.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1 -i make_bid_with_criteria_by_provider_first_stage -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_qualification_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на пре-кваліфікацію учасника створена помилково")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_qualification_mistaken.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1 -i make_bid_with_criteria_by_provider_first_stage -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_qualification_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на пре-кваліфікацію учасника залишена без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_qualification_invalid.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1 -i make_bid_with_criteria_by_provider_first_stage -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_qualification_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на пре-кваліфікацію учасника відхилена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_qualification_declined.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1 -i make_bid_with_criteria_by_provider_first_stage -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_qualification_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на пре-кваліфікацію учасника розгляд зупинено")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_qualification_stopped.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1 -i make_bid_with_criteria_by_provider_first_stage -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_lot_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота виконана Замовником")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_resolved.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_lot_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота створена помилково")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_mistaken.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_lot_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота залишена без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_invalid.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_lot_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота відхилена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_declined.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_lot_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота розгляд зупинено")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_stopped.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_cancel_tender_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера задоволена та виконана Замовником")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_resolved.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_cancel_tender_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера створена помилково")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_mistaken.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_cancel_tender_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера залишена без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_invalid.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_cancel_tender_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера відхилена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_declined.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_cancel_tender_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера розгляд зупинено")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_stopped.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_cancel_lot_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота задоволена та виконана Замовником")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_resolved.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_cancel_lot_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота створена помилково")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_mistaken.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_cancel_lot_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота залишена без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_invalid.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_cancel_lot_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота відхилена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_declined.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_cancel_lot_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота розгляд зупинено")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_stopped.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

//////////////////////////////////////////////////////////////////////////////////////////////////
//                  FULL COMPLAINTS TESTING (COMPETITIVE_DIALOGUE_UA)
//////////////////////////////////////////////////////////////////////////////////////////////////

    job("complaint_competitive_dialogue_ua_tender_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, прийнята та задоволена АМКУ та Учасником виконано рішення АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_resolved.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_tender_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, позначена Учасником як помилково створена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_mistaken.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_tender_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, прийнята до розгляду та відхилена АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_declined.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_tender_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, прийнята, розгляду зупинено АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_stopped.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints  $defaultArgs $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_tender_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, АМКУ залишив скаргу без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_invalid.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints  $defaultArgs $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_qualification_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на пре-кваліфікацію учасника задоволена Замовником")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_qualification_resolved.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1 -i make_bid_with_criteria_by_provider_first_stage -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_qualification_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на пре-кваліфікацію учасника створена помилково")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_qualification_mistaken.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1 -i make_bid_with_criteria_by_provider_first_stage -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_qualification_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на пре-кваліфікацію учасника залишена без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_qualification_invalid.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1 -i make_bid_with_criteria_by_provider_first_stage -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_qualification_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на пре-кваліфікацію учасника відхилена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_qualification_declined.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1 -i make_bid_with_criteria_by_provider_first_stage -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_qualification_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на пре-кваліфікацію учасника розгляд зупинено")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_qualification_stopped.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1 -i make_bid_with_criteria_by_provider_first_stage -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_lot_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота виконана Замовником")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_resolved.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_lot_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота створена помилково")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_mistaken.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_lot_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота залишена без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_invalid.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_lot_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота відхилена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_declined.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_lot_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота розгляд зупинено")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_stopped.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_cancel_tender_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера задоволена та виконана Замовником")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_resolved.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_cancel_tender_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера створена помилково")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_mistaken.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_cancel_tender_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера залишена без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_invalid.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_cancel_tender_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера відхилена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_declined.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_cancel_tender_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера розгляд зупинено")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_stopped.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_cancel_lot_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота задоволена та виконана Замовником")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_resolved.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_cancel_lot_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота створена помилково")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_mistaken.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_cancel_lot_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота залишена без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_invalid.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_cancel_lot_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота відхилена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_declined.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_cancel_lot_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота розгляд зупинено")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_stopped.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

//////////////////////////////////////////////////////////////////////////////////////////////////
//                  FULL COMPLAINTS TESTING (CLOSE_FRAMEWORK_AGREEMENT_UA)
//////////////////////////////////////////////////////////////////////////////////////////////////

     job("complaint_close_framework_agreement_ua_tender_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, прийнята та задоволена АМКУ та Учасником виконано рішення АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_resolved.txt"
        String mode = "-v MODE:closeFrameworkAgreementUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $close_framework_agreement_ua_acceleration $params")
            shell(shellRebot)
        }
    }

    job("complaint_close_framework_agreement_ua_tender_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, позначена Учасником як помилково створена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_mistaken.txt"
        String mode = "-v MODE:closeFrameworkAgreementUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $close_framework_agreement_ua_acceleration $params")
            shell(shellRebot)
        }
    }

    job("complaint_close_framework_agreement_ua_tender_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, прийнята до розгляду та відхилена АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_declined.txt"
        String mode = "-v MODE:closeFrameworkAgreementUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $close_framework_agreement_ua_acceleration $params")
            shell(shellRebot)
        }
    }

    job("complaint_close_framework_agreement_ua_tender_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, прийнята, розгляду зупинено АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_stopped.txt"
        String mode = "-v MODE:closeFrameworkAgreementUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $complaints  $defaultArgs $mode $close_framework_agreement_ua_acceleration $params")
            shell(shellRebot)
        }
    }

    job("complaint_close_framework_agreement_ua_tender_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, АМКУ залишив скаргу без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_invalid.txt"
        String mode = "-v MODE:closeFrameworkAgreementUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $complaints  $defaultArgs $mode $close_framework_agreement_ua_acceleration $params")
            shell(shellRebot)
        }
    }

    job("complaint_close_framework_agreement_ua_award_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на визначення переможця, прийнята та задоволена АМКУ та Учасником виконано рішення АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_award_resolved.txt"
        String pre_qualification = "-i make_bid_with_criteria_by_provider2 -i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid  -i pre-qualification_approve_qualifications -i pre-qualification_view"
        String mode = "-v MODE:closeFrameworkAgreementUA"
        String award = "-i qualification_approve_second_award -i qualification_approve_third_award -i qualification_approve_qualifications"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $complaints $defaultArgs $pre_qualification $award $mode $no_auction $close_framework_agreement_ua_acceleration $params")
            shell(shellRebot)
        }
    }

    job("complaint_close_framework_agreement_ua_award_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на визначення переможця, позначена Учасником як помилково створена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

         String defaultArgs = "-A robot_tests_arguments/complaint_award_mistaken.txt"
         String openeu_pre_qualification = "-i make_bid_with_criteria_by_provider2 -i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid  -i pre-qualification_approve_qualifications -i pre-qualification_view"
         String mode = "-v MODE:closeFrameworkAgreementUA"
         String award = "-i qualification_approve_second_award -i qualification_approve_third_award -i qualification_approve_qualifications"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $complaints $defaultArgs $openeu_pre_qualification $award $mode $no_auction $close_framework_agreement_ua_acceleration $params")
            shell(shellRebot)
        }
    }

    job("complaint_close_framework_agreement_ua_award_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на визначення переможця, прийнята до розгляду та відхилена АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_award_declined.txt"
        String openeu_pre_qualification = "-i make_bid_with_criteria_by_provider2 -i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid  -i pre-qualification_approve_qualifications -i pre-qualification_view"
        String mode = "-v MODE:closeFrameworkAgreementUA"
        String award = "-i qualification_approve_second_award -i qualification_approve_third_award -i qualification_approve_qualifications"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $complaints $defaultArgs $openeu_pre_qualification $award $mode $no_auction $close_framework_agreement_ua_acceleration $params")
            shell(shellRebot)
        }
    }

    job("complaint_close_framework_agreement_ua_award_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на визначення переможця, прийнята, розгляду зупинено АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_award_stopped.txt"
        String openeu_pre_qualification = "-i make_bid_with_criteria_by_provider2 -i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid  -i pre-qualification_approve_qualifications -i pre-qualification_view"
        String mode = "-v MODE:closeFrameworkAgreementUA"
        String award = "-i qualification_approve_second_award -i qualification_approve_third_award -i qualification_approve_qualifications"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $complaints $defaultArgs $openeu_pre_qualification $award $mode $no_auction $close_framework_agreement_ua_acceleration $params")
            shell(shellRebot)
        }
    }

    job("complaint_close_framework_agreement_ua_award_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на визначення переможця, АМКУ залишив скаргу без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_award_invalid.txt"
        String openeu_pre_qualification = "-i make_bid_with_criteria_by_provider2 -i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid  -i pre-qualification_approve_qualifications -i pre-qualification_view"
        String mode = "-v MODE:closeFrameworkAgreementUA"
        String award = "-i qualification_approve_second_award -i qualification_approve_third_award -i qualification_approve_qualifications"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $complaints $defaultArgs $openeu_pre_qualification $award $mode $no_auction $close_framework_agreement_ua_acceleration $params")
            shell(shellRebot)
        }
    }

    job("complaint_close_framework_agreement_ua_qualification_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на пре-кваліфікацію учасника задоволена Замовником")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_qualification_resolved.txt"
        String bid_pre_qualification = "-i make_bid_with_criteria_by_provider2 -i pre-qualification_approve_third_bid"
        String mode = "-v MODE:closeFrameworkAgreementUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $bid_pre_qualification $close_framework_agreement_ua_acceleration $params")
            shell(shellRebot)
        }
    }

    job("complaint_close_framework_agreement_ua_qualification_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на пре-кваліфікацію учасника створена помилково")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_qualification_mistaken.txt"
        String bid_pre_qualification = "-i make_bid_with_criteria_by_provider2 -i pre-qualification_approve_third_bid"
        String mode = "-v MODE:closeFrameworkAgreementUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $bid_pre_qualification $close_framework_agreement_ua_acceleration $params")
            shell(shellRebot)
        }
    }

    job("complaint_close_framework_agreement_ua_qualification_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на пре-кваліфікацію учасника залишена без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_qualification_invalid.txt"
        String bid_pre_qualification = "-i make_bid_with_criteria_by_provider2 -i pre-qualification_approve_third_bid"
        String mode = "-v MODE:closeFrameworkAgreementUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $bid_pre_qualification $close_framework_agreement_ua_acceleration $params")
            shell(shellRebot)
        }
    }

    job("complaint_close_framework_agreement_ua_qualification_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на пре-кваліфікацію учасника відхилена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_qualification_declined.txt"
        String bid_pre_qualification = "-i make_bid_with_criteria_by_provider2 -i pre-qualification_approve_third_bid"
        String mode = "-v MODE:closeFrameworkAgreementUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $bid_pre_qualification $close_framework_agreement_ua_acceleration $params")
            shell(shellRebot)
        }
    }

    job("complaint_close_framework_agreement_ua_qualification_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на пре-кваліфікацію учасника розгляд зупинено")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_qualification_stopped.txt"
        String bid_pre_qualification = "-i make_bid_with_criteria_by_provider2 -i pre-qualification_approve_third_bid"
        String mode = "-v MODE:closeFrameworkAgreementUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $bid_pre_qualification $close_framework_agreement_ua_acceleration $params")
            shell(shellRebot)
        }
    }

    job("complaint_close_framework_agreement_ua_lot_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота виконана Замовником")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_resolved.txt"
        String mode = "-v MODE:closeFrameworkAgreementUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $close_framework_agreement_ua_acceleration $params")
            shell(shellRebot)
        }
    }

    job("complaint_close_framework_agreement_ua_lot_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота створена помилково")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_mistaken.txt"
        String mode = "-v MODE:closeFrameworkAgreementUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $close_framework_agreement_ua_acceleration $params")
            shell(shellRebot)
        }
    }

    job("complaint_close_framework_agreement_ua_lot_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота залишена без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_invalid.txt"
        String mode = "-v MODE:closeFrameworkAgreementUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $close_framework_agreement_ua_acceleration $params")
            shell(shellRebot)
        }
    }

    job("complaint_close_framework_agreement_ua_lot_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота відхилена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_declined.txt"
        String mode = "-v MODE:closeFrameworkAgreementUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $close_framework_agreement_ua_acceleration $params")
            shell(shellRebot)
        }
    }

    job("complaint_close_framework_agreement_ua_lot_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота розгляд зупинено")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_stopped.txt"
        String mode = "-v MODE:closeFrameworkAgreementUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $close_framework_agreement_ua_acceleration $params")
            shell(shellRebot)
        }
    }

    job("complaint_close_framework_agreement_ua_cancel_tender_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера задоволена та виконана Замовником")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_resolved.txt"
        String mode = "-v MODE:closeFrameworkAgreementUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $close_framework_agreement_ua_acceleration $params")
            shell(shellRebot)
        }
    }

    job("complaint_close_framework_agreement_ua_cancel_tender_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера створена помилково")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_mistaken.txt"
        String mode = "-v MODE:closeFrameworkAgreementUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $close_framework_agreement_ua_acceleration $params")
            shell(shellRebot)
        }
    }

    job("complaint_close_framework_agreement_ua_cancel_tender_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера залишена без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_invalid.txt"
        String mode = "-v MODE:closeFrameworkAgreementUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $close_framework_agreement_ua_acceleration $params")
            shell(shellRebot)
        }
    }

    job("complaint_close_framework_agreement_ua_cancel_tender_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера відхилена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_declined.txt"
        String mode = "-v MODE:closeFrameworkAgreementUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $close_framework_agreement_ua_acceleration $params")
            shell(shellRebot)
        }
    }

    job("complaint_close_framework_agreement_ua_cancel_tender_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера розгляд зупинено")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_stopped.txt"
        String mode = "-v MODE:closeFrameworkAgreementUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $close_framework_agreement_ua_acceleration $params")
            shell(shellRebot)
        }
    }

    job("complaint_close_framework_agreement_ua_cancel_lot_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота задоволена та виконана Замовником")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_resolved.txt"
        String mode = "-v MODE:closeFrameworkAgreementUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode -v NUMBER_OF_LOTS:1 $close_framework_agreement_ua_acceleration $params")
            shell(shellRebot)
        }
    }

    job("complaint_close_framework_agreement_ua_cancel_lot_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота створена помилково")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_mistaken.txt"
        String mode = "-v MODE:closeFrameworkAgreementUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $close_framework_agreement_ua_acceleration $params")
            shell(shellRebot)
        }
    }

    job("complaint_close_framework_agreement_ua_cancel_lot_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота залишена без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_invalid.txt"
        String mode = "-v MODE:closeFrameworkAgreementUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $close_framework_agreement_ua_acceleration $params")
            shell(shellRebot)
        }
    }

    job("complaint_close_framework_agreement_ua_cancel_lot_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота відхилена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_declined.txt"
        String mode = "-v MODE:closeFrameworkAgreementUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $close_framework_agreement_ua_acceleration $params")
            shell(shellRebot)
        }
    }

    job("complaint_close_framework_agreement_ua_cancel_lot_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота розгляд зупинено")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_stopped.txt"
        String mode = "-v MODE:closeFrameworkAgreementUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $close_framework_agreement_ua_acceleration $params")
            shell(shellRebot)
        }
    }

 job("complaint_competitive_dialogue_eu_stage_2_tender_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, прийнята та задоволена АМКУ та Учасником виконано рішення АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_resolved.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1  -i make_bid_with_criteria_by_provider_first_stage  -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid -i pre-qualification_approve_qualifications"
        String stage_2 = "-i stage2_pending_status_view -i wait_bridge_for_work -i get_second_stage -i compare_stages -i save_tender_second_stage -i activate_second_stage"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $stage_2 $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_stage_2_tender_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, позначена Учасником як помилково створена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_mistaken.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1  -i make_bid_with_criteria_by_provider_first_stage  -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid -i pre-qualification_approve_qualifications"
        String stage_2 = "-i stage2_pending_status_view -i wait_bridge_for_work -i get_second_stage -i compare_stages -i save_tender_second_stage -i activate_second_stage"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $stage_2 $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_stage_2_tender_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, прийнята до розгляду та відхилена АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_declined.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1  -i make_bid_with_criteria_by_provider_first_stage  -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid -i pre-qualification_approve_qualifications"
        String stage_2 = "-i stage2_pending_status_view -i wait_bridge_for_work -i get_second_stage -i compare_stages -i save_tender_second_stage -i activate_second_stage"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $stage_2 $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_stage_2_tender_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, прийнята, розгляду зупинено АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_stopped.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1  -i make_bid_with_criteria_by_provider_first_stage  -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid -i pre-qualification_approve_qualifications"
        String stage_2 = "-i stage2_pending_status_view -i wait_bridge_for_work -i get_second_stage -i compare_stages -i save_tender_second_stage -i activate_second_stage"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints  $defaultArgs $bidding $pre_qualification $stage_2 $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_stage_2_tender_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, АМКУ залишив скаргу без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_invalid.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1  -i make_bid_with_criteria_by_provider_first_stage  -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid -i pre-qualification_approve_qualifications"
        String stage_2 = "-i stage2_pending_status_view -i wait_bridge_for_work -i get_second_stage -i compare_stages -i save_tender_second_stage -i activate_second_stage"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints  $defaultArgs $bidding $pre_qualification $stage_2 $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_stage_2_lot_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота виконана Замовником")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_resolved.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1  -i make_bid_with_criteria_by_provider_first_stage  -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid -i pre-qualification_approve_qualifications"
        String stage_2 = "-i stage2_pending_status_view -i wait_bridge_for_work -i get_second_stage -i compare_stages -i save_tender_second_stage -i activate_second_stage"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $stage_2 $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_stage_2_lot_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота створена помилково")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_mistaken.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1  -i make_bid_with_criteria_by_provider_first_stage  -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid -i pre-qualification_approve_qualifications"
        String stage_2 = "-i stage2_pending_status_view -i wait_bridge_for_work -i get_second_stage -i compare_stages -i save_tender_second_stage -i activate_second_stage"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $stage_2 $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_stage_2_lot_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота залишена без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_invalid.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1  -i make_bid_with_criteria_by_provider_first_stage  -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid -i pre-qualification_approve_qualifications"
        String stage_2 = "-i stage2_pending_status_view -i wait_bridge_for_work -i get_second_stage -i compare_stages -i save_tender_second_stage -i activate_second_stage"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $stage_2 $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_stage_2_lot_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота відхилена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_declined.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1  -i make_bid_with_criteria_by_provider_first_stage  -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid -i pre-qualification_approve_qualifications"
        String stage_2 = "-i stage2_pending_status_view -i wait_bridge_for_work -i get_second_stage -i compare_stages -i save_tender_second_stage -i activate_second_stage"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $stage_2 $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_stage_2_lot_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота розгляд зупинено")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_stopped.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1  -i make_bid_with_criteria_by_provider_first_stage  -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid -i pre-qualification_approve_qualifications"
        String stage_2 = "-i stage2_pending_status_view -i wait_bridge_for_work -i get_second_stage -i compare_stages -i save_tender_second_stage -i activate_second_stage"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $stage_2 $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_stage_2_cancel_tender_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера задоволена та виконана Замовником")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_resolved.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1  -i make_bid_with_criteria_by_provider_first_stage  -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid -i pre-qualification_approve_qualifications"
        String stage_2 = "-i stage2_pending_status_view -i wait_bridge_for_work -i get_second_stage -i compare_stages -i save_tender_second_stage -i activate_second_stage"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $stage_2 $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_stage_2_cancel_tender_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера створена помилково")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_mistaken.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1  -i make_bid_with_criteria_by_provider_first_stage  -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid -i pre-qualification_approve_qualifications"
        String stage_2 = "-i stage2_pending_status_view -i wait_bridge_for_work -i get_second_stage -i compare_stages -i save_tender_second_stage -i activate_second_stage"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $stage_2 $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_stage_2_cancel_tender_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера залишена без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_invalid.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1  -i make_bid_with_criteria_by_provider_first_stage  -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid -i pre-qualification_approve_qualifications"
        String stage_2 = "-i stage2_pending_status_view -i wait_bridge_for_work -i get_second_stage -i compare_stages -i save_tender_second_stage -i activate_second_stage"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $stage_2 $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_stage_2_cancel_tender_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера відхилена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_declined.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1  -i make_bid_with_criteria_by_provider_first_stage  -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid -i pre-qualification_approve_qualifications"
        String stage_2 = "-i stage2_pending_status_view -i wait_bridge_for_work -i get_second_stage -i compare_stages -i save_tender_second_stage -i activate_second_stage"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $stage_2 $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_eu_stage_2_cancel_tender_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера розгляд зупинено")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_stopped.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1  -i make_bid_with_criteria_by_provider_first_stage  -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid -i pre-qualification_approve_qualifications"
        String stage_2 = "-i stage2_pending_status_view -i wait_bridge_for_work -i get_second_stage -i compare_stages -i save_tender_second_stage -i activate_second_stage"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $stage_2 $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_stage_2_tender_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, прийнята та задоволена АМКУ та Учасником виконано рішення АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_resolved.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1  -i make_bid_with_criteria_by_provider_first_stage  -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid -i pre-qualification_approve_qualifications"
        String stage_2 = "-i stage2_pending_status_view -i wait_bridge_for_work -i get_second_stage -i compare_stages -i save_tender_second_stage -i activate_second_stage"


        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $stage_2 $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_stage_2_tender_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, позначена Учасником як помилково створена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_mistaken.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1  -i make_bid_with_criteria_by_provider_first_stage  -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid -i pre-qualification_approve_qualifications"
        String stage_2 = "-i stage2_pending_status_view -i wait_bridge_for_work -i get_second_stage -i compare_stages -i save_tender_second_stage -i activate_second_stage"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $stage_2 $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_stage_2_tender_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, прийнята до розгляду та відхилена АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_declined.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1  -i make_bid_with_criteria_by_provider_first_stage  -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid -i pre-qualification_approve_qualifications"
        String stage_2 = "-i stage2_pending_status_view -i wait_bridge_for_work -i get_second_stage -i compare_stages -i save_tender_second_stage -i activate_second_stage"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $stage_2 $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_stage_2_tender_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, прийнята, розгляду зупинено АМКУ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_stopped.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1  -i make_bid_with_criteria_by_provider_first_stage  -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid -i pre-qualification_approve_qualifications"
        String stage_2 = "-i stage2_pending_status_view -i wait_bridge_for_work -i get_second_stage -i compare_stages -i save_tender_second_stage -i activate_second_stage"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints  $defaultArgs $bidding $pre_qualification $stage_2 $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_stage_2_tender_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови тендера, АМКУ залишив скаргу без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_tender_invalid.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1  -i make_bid_with_criteria_by_provider_first_stage  -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid -i pre-qualification_approve_qualifications"
        String stage_2 = "-i stage2_pending_status_view -i wait_bridge_for_work -i get_second_stage -i compare_stages -i save_tender_second_stage -i activate_second_stage"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints  $defaultArgs $bidding $pre_qualification $stage_2 $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_stage_2_lot_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота виконана Замовником")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_resolved.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1  -i make_bid_with_criteria_by_provider_first_stage  -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid -i pre-qualification_approve_qualifications"
        String stage_2 = "-i stage2_pending_status_view -i wait_bridge_for_work -i get_second_stage -i compare_stages -i save_tender_second_stage -i activate_second_stage"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $stage_2 $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_stage_2_lot_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота створена помилково")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_mistaken.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1  -i make_bid_with_criteria_by_provider_first_stage  -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid -i pre-qualification_approve_qualifications"
        String stage_2 = "-i stage2_pending_status_view -i wait_bridge_for_work -i get_second_stage -i compare_stages -i save_tender_second_stage -i activate_second_stage"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $stage_2 $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_stage_2_lot_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота залишена без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_invalid.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1  -i make_bid_with_criteria_by_provider_first_stage  -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid -i pre-qualification_approve_qualifications"
        String stage_2 = "-i stage2_pending_status_view -i wait_bridge_for_work -i get_second_stage -i compare_stages -i save_tender_second_stage -i activate_second_stage"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $stage_2 $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_stage_2_lot_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота відхилена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_declined.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1  -i make_bid_with_criteria_by_provider_first_stage  -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid -i pre-qualification_approve_qualifications"
        String stage_2 = "-i stage2_pending_status_view -i wait_bridge_for_work -i get_second_stage -i compare_stages -i save_tender_second_stage -i activate_second_stage"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $stage_2 $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_stage_2_lot_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на умови лота розгляд зупинено")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_lot_stopped.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1  -i make_bid_with_criteria_by_provider_first_stage  -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid -i pre-qualification_approve_qualifications"
        String stage_2 = "-i stage2_pending_status_view -i wait_bridge_for_work -i get_second_stage -i compare_stages -i save_tender_second_stage -i activate_second_stage"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $stage_2 $bidding $pre_qualification $stage_2 $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

     job("complaint_competitive_dialogue_ua_stage_2_cancel_tender_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера задоволена та виконана Замовником")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_resolved.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1  -i make_bid_with_criteria_by_provider_first_stage  -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid -i pre-qualification_approve_qualifications"
        String stage_2 = "-i stage2_pending_status_view -i wait_bridge_for_work -i get_second_stage -i compare_stages -i save_tender_second_stage -i activate_second_stage"


        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $stage_2 $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_stage_2_cancel_tender_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера створена помилково")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_mistaken.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1  -i make_bid_with_criteria_by_provider_first_stage  -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid -i pre-qualification_approve_qualifications"
        String stage_2 = "-i stage2_pending_status_view -i wait_bridge_for_work -i get_second_stage -i compare_stages -i save_tender_second_stage -i activate_second_stage"


        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $stage_2 $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_stage_2_cancel_tender_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера залишена без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_invalid.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1  -i make_bid_with_criteria_by_provider_first_stage  -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid -i pre-qualification_approve_qualifications"
        String stage_2 = "-i stage2_pending_status_view -i wait_bridge_for_work -i get_second_stage -i compare_stages -i save_tender_second_stage -i activate_second_stage"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $stage_2 $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_stage_2_cancel_tender_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера відхилена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_declined.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1  -i make_bid_with_criteria_by_provider_first_stage  -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid -i pre-qualification_approve_qualifications"
        String stage_2 = "-i stage2_pending_status_view -i wait_bridge_for_work -i get_second_stage -i compare_stages -i save_tender_second_stage -i activate_second_stage"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $stage_2 $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_stage_2_cancel_tender_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування тендера розгляд зупинено")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_tender_stopped.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1  -i make_bid_with_criteria_by_provider_first_stage  -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid -i pre-qualification_approve_qualifications"
        String stage_2 = "-i stage2_pending_status_view -i wait_bridge_for_work -i get_second_stage -i compare_stages -i save_tender_second_stage -i activate_second_stage"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $stage_2 $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_stage_2_cancel_lot_resolved") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота задоволена та виконана Замовником")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_resolved.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1  -i make_bid_with_criteria_by_provider_first_stage  -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid -i pre-qualification_approve_qualifications"
        String stage_2 = "-i stage2_pending_status_view -i wait_bridge_for_work -i get_second_stage -i compare_stages -i save_tender_second_stage -i activate_second_stage"


        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $stage_2 $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_stage_2_cancel_lot_mistaken") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота створена помилково")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_mistaken.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1  -i make_bid_with_criteria_by_provider_first_stage  -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid -i pre-qualification_approve_qualifications"
        String stage_2 = "-i stage2_pending_status_view -i wait_bridge_for_work -i get_second_stage -i compare_stages -i save_tender_second_stage -i activate_second_stage"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $stage_2 $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_stage_2_cancel_lot_invalid") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота залишена без розгляду")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_invalid.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1  -i make_bid_with_criteria_by_provider_first_stage  -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid -i pre-qualification_approve_qualifications"
        String stage_2 = "-i stage2_pending_status_view -i wait_bridge_for_work -i get_second_stage -i compare_stages -i save_tender_second_stage -i activate_second_stage"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $stage_2 $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_stage_2_cancel_lot_declined") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота відхилена")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_declined.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1  -i make_bid_with_criteria_by_provider_first_stage  -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid -i pre-qualification_approve_qualifications"
        String stage_2 = "-i stage2_pending_status_view -i wait_bridge_for_work -i get_second_stage -i compare_stages -i save_tender_second_stage -i activate_second_stage"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $stage_2 $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("complaint_competitive_dialogue_ua_stage_2_cancel_lot_stopped") {
        parameters defaultParameters(config)
        description("Сценарій: Скарга на скасування лота розгляд зупинено")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/complaint_cancel_lot_stopped.txt"
        String accelerate_competitive_dialogue_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1  -i make_bid_with_criteria_by_provider_first_stage  -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid -i pre-qualification_approve_qualifications"
        String stage_2 = "-i stage2_pending_status_view -i wait_bridge_for_work -i get_second_stage -i compare_stages -i save_tender_second_stage -i activate_second_stage"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $stage_2 $mode $accelerate_competitive_dialogue_ua $params")
            shell(shellRebot)
        }
    }

    job("claims_aboveThresholdEU_draft_cancel") {
        parameters defaultParameters(config)
        description("Сценарій: ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/claims/claim_draft_cancel.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $claims $defaultArgs $no_auction $accelerate_openeu $params")
            shell(shellRebot)
        }
    }

    job("claims_aboveThresholdEU_draft_claim_answer_cancel") {
        parameters defaultParameters(config)
        description("Сценарій: ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/claims/claim_draft_claim_answer_cancel.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $claims $defaultArgs $no_auction $accelerate_openeu $params")
            shell(shellRebot)
        }
    }

    job("claims_aboveThresholdEU_draft_claim_answer_resolve") {
        parameters defaultParameters(config)
        description("Сценарій: ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/claims/claim_draft_claim_answer_resolve.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $claims $defaultArgs $no_auction $accelerate_openeu $params")
            shell(shellRebot)
        }
    }

    job("claims_aboveThresholdEU_draft_claim_cancel") {
        parameters defaultParameters(config)
        description("Сценарій: ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/claims/claim_draft_claim_cancel.txt"
        String accelerate_openeu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdEU $params")
            shell("$robotWrapper $claims $defaultArgs $no_auction $accelerate_openeu $params")
            shell(shellRebot)
        }
    }

    job("claims_aboveThresholdUA_draft_cancel") {
        parameters defaultParameters(config)
        description("Сценарій: ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/claims/claim_draft_cancel.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdUA\":{\"tender\":[1,10],\"accelerator\":2880}}}}'"
        String exclude_pre_qualification = "-e pre-qualification_approve_first_bid -e pre-qualification_approve_second_bid -e pre-qualification_approve_qualifications -e pre-qualification_view"
        String exclude_pre_qualification_claim = "-e qualification_claim_draft -e cancel_qualification_claim"
        String mode = "-v MODE:aboveThresholdUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $claims $defaultArgs $exclude_pre_qualification $exclude_pre_qualification_claim $mode $no_auction $accelerate_openua $params")
            shell(shellRebot)
        }
    }

    job("claims_aboveThresholdUA_draft_claim_answer_cancel") {
        parameters defaultParameters(config)
        description("Сценарій: ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/claims/claim_draft_claim_answer_cancel.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdUA\":{\"tender\":[1,10],\"accelerator\":2880}}}}'"
        String exclude_pre_qualification = "-e pre-qualification_approve_first_bid -e pre-qualification_approve_second_bid -e pre-qualification_approve_qualifications -e pre-qualification_view"
        String exclude_pre_qualification_claim = "-e qualification_claim_draft -e submit_qualification_claim -e answer_qualification_claim -e cancel_qualification_claim"
        String mode = "-v MODE:aboveThresholdUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $claims $defaultArgs $exclude_pre_qualification $exclude_pre_qualification_claim $mode $no_auction $accelerate_openua $params")
            shell(shellRebot)
        }
    }

    job("claims_aboveThresholdUA_draft_claim_answer_resolve") {
        parameters defaultParameters(config)
        description("Сценарій: ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/claims/claim_draft_claim_answer_resolve.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdUA\":{\"tender\":[1,10],\"accelerator\":2880}}}}'"
        String exclude_pre_qualification = "-e pre-qualification_approve_first_bid -e pre-qualification_approve_second_bid -e pre-qualification_approve_qualifications -e pre-qualification_view"
        String exclude_pre_qualification_claim = "-e qualification_claim_draft -e submit_qualification_claim -e answer_qualification_claim -e cancel_qualification_claim"
        String mode = "-v MODE:aboveThresholdUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $claims $defaultArgs $exclude_pre_qualification $exclude_pre_qualification_claim $mode $no_auction $accelerate_openua $params")
            shell(shellRebot)
        }
    }

    job("claims_aboveThresholdUA_draft_claim_cancel") {
        parameters defaultParameters(config)
        description("Сценарій: ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/claims/claim_draft_claim_cancel.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"aboveThresholdUA\":{\"tender\":[1,10],\"accelerator\":2880}}}}'"
        String exclude_pre_qualification = "-e pre-qualification_approve_first_bid -e pre-qualification_approve_second_bid -e pre-qualification_approve_qualifications -e pre-qualification_view"
        String exclude_pre_qualification_claim = "-e qualification_claim_draft -e submit_qualification_claim -e cancel_qualification_claim"
        String mode = "-v MODE:aboveThresholdUA"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:aboveThresholdUA $params")
            shell("$robotWrapper $claims $defaultArgs $exclude_pre_qualification $exclude_pre_qualification_claim $mode $no_auction $accelerate_openua $params")
            shell(shellRebot)
        }
    }

    job("claims_competitiveDialogueEU_draft_cancel") {
        parameters defaultParameters(config)
        description("Сценарій: ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/claims/claim_draft_cancel.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"
        String bid = "-i make_bid_with_criteria_by_provider2"
        String pre_qualification = "-i pre-qualification_approve_third_bid"
        String exclude_award = "-e awardPeriod_startDate -e qualification_approve_first_award"
        String exclude_award_claim = "-e award_claim_draft -e cancel_award_claim"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $claims $defaultArgs $bid $pre_qualification $exclude_award $exclude_award_claim $mode $no_auction $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("claims_competitiveDialogueEU_draft_claim_answer_cancel") {
        parameters defaultParameters(config)
        description("Сценарій: ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/claims/claim_draft_claim_answer_cancel.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,10],\"accelerator\":4320}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"
        String bid = "-i make_bid_with_criteria_by_provider2"
        String pre_qualification = "-i pre-qualification_approve_third_bid"
        String exclude_award = "-e awardPeriod_startDate -e qualification_approve_first_award"
        String exclude_award_claim = "-e award_claim_draft -e submit_award_claim -e answer_award_claim -e cancel_award_claim"


        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $claims $defaultArgs $bid $pre_qualification $exclude_award $exclude_award_claim $mode $no_auction $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("claims_competitiveDialogueEU_draft_claim_answer_resolve") {
        parameters defaultParameters(config)
        description("Сценарій: ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/claims/claim_draft_claim_answer_resolve.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,10],\"accelerator\":4320}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"
        String bid = "-i make_bid_with_criteria_by_provider2"
        String pre_qualification = "-i pre-qualification_approve_third_bid"
        String exclude_award = "-e awardPeriod_startDate -e qualification_approve_first_award"
        String exclude_award_claim = "-e award_claim_draft -e submit_award_claim -e answer_award_claim"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $claims $defaultArgs $bid $pre_qualification $exclude_award $exclude_award_claim $mode $no_auction $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("claims_competitiveDialogueEU_draft_claim_cancel") {
        parameters defaultParameters(config)
        description("Сценарій: ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/claims/claim_draft_claim_cancel.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueEU\":{\"tender\":[1,10],\"accelerator\":4320}}}}'"
        String mode = "-v MODE:competitiveDialogueEU"
        String bid = "-i make_bid_with_criteria_by_provider2"
        String pre_qualification = "-i pre-qualification_approve_third_bid"
        String exclude_award = "-e awardPeriod_startDate -e qualification_approve_first_award"
        String exclude_award_claim = "-e award_claim_draft -e submit_award_claim -e cancel_award_claim"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $claims $defaultArgs $bid $pre_qualification $exclude_award $exclude_award_claim $mode $no_auction $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("claims_competitiveDialogueUA_draft_cancel") {
        parameters defaultParameters(config)
        description("Сценарій: ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/claims/claim_draft_cancel.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"
        String bid = "-i make_bid_with_criteria_by_provider2"
        String pre_qualification = "-i pre-qualification_approve_third_bid"
        String exclude_award = "-e awardPeriod_startDate -e qualification_approve_first_award"
        String exclude_award_claim = "-e award_claim_draft -e cancel_award_claim"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $claims $defaultArgs $bid $pre_qualification $exclude_award $exclude_award_claim $mode $no_auction $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("claims_competitiveDialogueUA_draft_claim_answer_cancel") {
        parameters defaultParameters(config)
        description("Сценарій: ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/claims/claim_draft_claim_answer_cancel.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,10],\"accelerator\":4320}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"
        String bid = "-i make_bid_with_criteria_by_provider2"
        String pre_qualification = "-i pre-qualification_approve_third_bid"
        String exclude_award = "-e awardPeriod_startDate -e qualification_approve_first_award"
        String exclude_award_claim = "-e award_claim_draft -e submit_award_claim -e answer_award_claim -e cancel_award_claim"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $claims $defaultArgs $bid $pre_qualification $exclude_award $exclude_award_claim $mode $no_auction accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("claims_competitiveDialogueUA_draft_claim_answer_resolve") {
        parameters defaultParameters(config)
        description("Сценарій: ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/claims/claim_draft_claim_answer_resolve.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,10],\"accelerator\":4320}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"
        String bid = "-i make_bid_with_criteria_by_provider2"
        String pre_qualification = "-i pre-qualification_approve_third_bid"
        String exclude_award = "-e awardPeriod_startDate -e qualification_approve_first_award"
        String exclude_award_claim = "-e award_claim_draft -e submit_award_claim -e answer_award_claim"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $claims $defaultArgs $bid $pre_qualification $exclude_award $exclude_award_claim $mode $no_auction $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("claims_competitiveDialogueUA_draft_claim_cancel") {
        parameters defaultParameters(config)
        description("Сценарій: ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/claims/claim_draft_claim_cancel.txt"
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"competitiveDialogueUA\":{\"tender\":[1,10],\"accelerator\":4320}}}}'"
        String mode = "-v MODE:competitiveDialogueUA"
        String bid = "-i make_bid_with_criteria_by_provider2"
        String pre_qualification = "-i pre-qualification_approve_third_bid"
        String exclude_award = "-e awardPeriod_startDate -e qualification_approve_first_award"
        String exclude_award_claim = "-e award_claim_draft -e submit_award_claim -e cancel_award_claim"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueUA $params")
            shell("$robotWrapper $claims $bid $pre_qualification $defaultArgs $exclude_award $exclude_award_claim $mode $no_auction $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("claims_esco_draft_cancel") {
        parameters defaultParameters(config)
        description("Сценарій: ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/claims/claim_draft_cancel.txt"
        String accelerate_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"tender\":[1,10],\"accelerator\":4320}}}}'"
        String mode = "-v MODE:esco"
        String fundingKind = "-v FUNDING_KIND:budget"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $claims $defaultArgs $mode $fundingKind $no_auction $accelerate_esco $params")
            shell(shellRebot)
        }
    }

    job("claims_esco_draft_claim_answer_cancel") {
        parameters defaultParameters(config)
        description("Сценарій: ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/claims/claim_draft_claim_answer_cancel.txt"
        String accelerate_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"tender\":[1,10],\"accelerator\":4320}}}}'"
        String mode = "-v MODE:esco"
        String fundingKind = "-v FUNDING_KIND:budget"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $claims $defaultArgs $mode $fundingKind $no_auction $accelerate_esco $params")
            shell(shellRebot)
        }
    }

    job("claims_esco_draft_claim_answer_resolve") {
        parameters defaultParameters(config)
        description("Сценарій: ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/claims/claim_draft_claim_answer_resolve.txt"
        String accelerate_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"tender\":[1,10],\"accelerator\":4320}}}}'"
        String mode = "-v MODE:esco"
        String fundingKind = "-v FUNDING_KIND:budget"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $claims $defaultArgs $mode $fundingKind $no_auction $accelerate_esco $params")
            shell(shellRebot)
        }
    }

    job("claims_esco_draft_claim_cancel") {
        parameters defaultParameters(config)
        description("Сценарій: ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/claims/claim_draft_claim_cancel.txt"
        String accelerate_esco = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"esco\":{\"tender\":[1,10],\"accelerator\":4320}}}}'"
        String mode = "-v MODE:esco"
        String fundingKind = "-v FUNDING_KIND:budget"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:esco $params")
            shell("$robotWrapper $claims $defaultArgs $mode $fundingKind $no_auction $accelerate_esco $params")
            shell(shellRebot)
        }
    }

    job("claims_closeFrameworkAgreementUA_draft_cancel") {
        parameters defaultParameters(config)
        description("Сценарій: ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/claims/claim_draft_cancel.txt"
        String accelerate_close_framework_agreement_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"closeFrameworkAgreementUA\":{\"tender\":[1,10],\"accelerator\":4320}}}}'"
        String mode = "-v MODE:closeFrameworkAgreementUA"
        String bid = "-i make_bid_with_criteria_by_provider2"
        String pre_qualification = "-i pre-qualification_approve_third_bid"
        String exclude_award = "-e awardPeriod_startDate -e qualification_approve_first_award"
        String exclude_award_claim = "-e award_claim_draft -e cancel_award_claim"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $claims $defaultArgs $bid $pre_qualification $exclude_award $exclude_award_claim $mode $no_auction $accelerate_close_framework_agreement_ua $params")
            shell(shellRebot)
        }
    }

    job("claims_closeFrameworkAgreementUA_draft_claim_answer_cancel") {
        parameters defaultParameters(config)
        description("Сценарій: ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/claims/claim_draft_claim_answer_cancel.txt"
        String accelerate_close_framework_agreement_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"closeFrameworkAgreementUA\":{\"tender\":[1,10],\"accelerator\":4320}}}}'"
        String mode = "-v MODE:closeFrameworkAgreementUA"
        String bid = "-i make_bid_with_criteria_by_provider2"
        String pre_qualification = "-i pre-qualification_approve_third_bid"
        String exclude_award = "-e awardPeriod_startDate -e qualification_approve_first_award"
        String exclude_award_claim = "-e award_claim_draft -e submit_award_claim -e answer_award_claim -e cancel_award_claim"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $claims $defaultArgs $bid $pre_qualification $exclude_award $exclude_award_claim $mode $no_auction $accelerate_close_framework_agreement_ua $params")
            shell(shellRebot)
        }
    }

    job("claims_closeFrameworkAgreementUA_draft_claim_answer_resolve") {
        parameters defaultParameters(config)
        description("Сценарій: ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/claims/claim_draft_claim_answer_resolve.txt"
        String accelerate_close_framework_agreement_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"closeFrameworkAgreementUA\":{\"tender\":[1,10],\"accelerator\":4320}}}}'"
        String mode = "-v MODE:closeFrameworkAgreementUA"
        String bid = "-i make_bid_with_criteria_by_provider2"
        String pre_qualification = "-i pre-qualification_approve_third_bid"
        String exclude_award = "-e awardPeriod_startDate -e qualification_approve_first_award"
        String exclude_award_claim = "-e award_claim_draft -e submit_award_claim -e answer_award_claim"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $claims $defaultArgs $bid $pre_qualification $exclude_award $exclude_award_claim $mode $no_auction $accelerate_close_framework_agreement_ua $params")
            shell(shellRebot)
        }
    }

    job("claims_closeFrameworkAgreementUA_draft_claim_cancel") {
        parameters defaultParameters(config)
        description("Сценарій: ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/claims/claim_draft_claim_cancel.txt"
        String accelerate_close_framework_agreement_ua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"closeFrameworkAgreementUA\":{\"tender\":[1,10],\"accelerator\":4320}}}}'"
        String mode = "-v MODE:closeFrameworkAgreementUA"
        String bid = "-i make_bid_with_criteria_by_provider2"
        String pre_qualification = "-i pre-qualification_approve_third_bid"
        String exclude_award = "-e awardPeriod_startDate -e qualification_approve_first_award"
        String exclude_award_claim = "-e award_claim_draft -e submit_award_claim -e cancel_award_claim"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:closeFrameworkAgreementUA $params")
            shell("$robotWrapper $claims $defaultArgs $bid $pre_qualification $exclude_award $exclude_award_claim $mode $no_auction $accelerate_close_framework_agreement_ua $params")
            shell(shellRebot)
        }
    }

    job("claims_simple_defense_draft_cancel") {
        parameters defaultParameters(config)
        description("Сценарій: ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/claims/claim_draft_cancel.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"simple.defense\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"
        String exclude_pre_qualification = "-e pre-qualification_approve_first_bid -e pre-qualification_approve_second_bid -e pre-qualification_approve_qualifications -e pre-qualification_view"
        String exclude_pre_qualification_claim = "-e qualification_claim_draft -e cancel_qualification_claim"
        String exclude_award = "-e awardPeriod_startDate -e qualification_approve_first_award"
        String exclude_award_claim = "-e award_claim_draft -e cancel_award_claim"
        String mode = "-v MODE:simple.defense"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $claims $defaultArgs $exclude_pre_qualification $exclude_pre_qualification_claim $exclude_award $exclude_award_claim $mode $no_auction $accelerate_openua $params")
            shell(shellRebot)
        }
    }

    job("claims_simple_defense_draft_claim_answer_cancel") {
        parameters defaultParameters(config)
        description("Сценарій: ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/claims/claim_draft_claim_answer_cancel.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"simple.defense\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"
        String exclude_pre_qualification = "-e pre-qualification_approve_first_bid -e pre-qualification_approve_second_bid -e pre-qualification_approve_qualifications -e pre-qualification_view"
        String exclude_pre_qualification_claim = "-e qualification_claim_draft -e submit_qualification_claim -e answer_qualification_claim -e cancel_qualification_claim"
        String exclude_award = "-e awardPeriod_startDate -e qualification_approve_first_award"
        String exclude_award_claim = "-e award_claim_draft -e submit_award_claim -e answer_award_claim -e cancel_award_claim"
        String mode = "-v MODE:simple.defense"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $claims $defaultArgs $exclude_pre_qualification $exclude_pre_qualification_claim $exclude_award $exclude_award_claim $mode $no_auction $accelerate_openua $params")
            shell(shellRebot)
        }
    }

    job("claims_simple_defense_draft_claim_answer_resolve") {
        parameters defaultParameters(config)
        description("Сценарій: ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/claims/claim_draft_claim_answer_resolve.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"simple.defense\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"
        String exclude_pre_qualification = "-e pre-qualification_approve_first_bid -e pre-qualification_approve_second_bid -e pre-qualification_approve_qualifications -e pre-qualification_view"
        String exclude_pre_qualification_claim = "-e qualification_claim_draft -e submit_qualification_claim -e answer_qualification_claim -e cancel_qualification_claim"
        String exclude_award = "-e awardPeriod_startDate -e qualification_approve_first_award"
        String exclude_award_claim = "-e award_claim_draft -e submit_award_claim -e answer_award_claim"
        String mode = "-v MODE:simple.defense"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $claims $defaultArgs $exclude_pre_qualification $exclude_pre_qualification_claim $exclude_award $exclude_award_claim $mode $no_auction $accelerate_openua $params")
            shell(shellRebot)
        }
    }

    job("claims_simple_defense_draft_claim_cancel") {
        parameters defaultParameters(config)
        description("Сценарій: ")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(config.concurrentBuild)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure
        environmentVariables defaultEnv()

        String defaultArgs = "-A robot_tests_arguments/claims/claim_draft_claim_cancel.txt"
        String accelerate_openua = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"simple.defense\":{\"tender\":[1,5],\"accelerator\":4320}}}}'"
        String exclude_pre_qualification = "-e pre-qualification_approve_first_bid -e pre-qualification_approve_second_bid -e pre-qualification_approve_qualifications -e pre-qualification_view"
        String exclude_pre_qualification_claim = "-e qualification_claim_draft -e submit_qualification_claim -e cancel_qualification_claim"
        String exclude_award = "-e awardPeriod_startDate -e qualification_approve_first_award"
        String exclude_award_claim = "-e award_claim_draft -e submit_award_claim -e cancel_award_claim"
        String mode = "-v MODE:simple.defense"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:simple.defense $params")
            shell("$robotWrapper $claims $defaultArgs $exclude_pre_qualification $exclude_pre_qualification_claim $exclude_award $exclude_award_claim $mode $no_auction $accelerate_openua $params")
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
                    "${config.environment}_competitiveDialogueEU_vat_true_false",
                    "${config.environment}_competitiveDialogueEU_vat_false_false",
                    "${config.environment}_competitiveDialogueEU_vat_false_true",
                    "${config.environment}_competitiveDialogueUA",
                    "${config.environment}_competitiveDialogueUA_vat_true_false",
                    "${config.environment}_competitiveDialogueUA_vat_false_false",
                    "${config.environment}_competitiveDialogueUA_vat_false_true",
                    "${config.environment}_negotiation",
                    "${config.environment}_negotiation.quick",
                    "${config.environment}_reporting",
                    "${config.environment}_single_item_tender",
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
                    "${config.environment}_planning_esco",
                    "${config.environment}_planning_reporting",
                    "${config.environment}_planning_negotiation",
                    "${config.environment}_planning_negotiation.quick",
                    "${config.environment}_plan_tender_validations",
                    "${config.environment}_planning_belowThreshold",
                    "${config.environment}_planning_framework_agreement",
                    "${config.environment}_aboveThresholdUA_24_hours_award",
                    "${config.environment}_aboveThresholdEU_24_hours_qualification",
                    "${config.environment}_aboveThresholdUA_alp",
                    "${config.environment}_aboveThresholdEU_EDR_DFS",
                    "${config.environment}_aboveThresholdUA_EDR_DFS",
                    "${config.environment}_competitiveDialogueEU_EDR_DFS",
                    "${config.environment}_competitiveDialogueUA_EDR_DFS",
                    "${config.environment}_esco_EDR_DFS",
                    "${config.environment}_simple_defence_one_bid",
                    "${config.environment}_simple_defence",
                    "${config.environment}_complaint_simple_defence_first_award_cancel",
                    "${config.environment}_complaint_simple_defence_first_award_disqualification",
                    "${config.environment}_complaint_simple_defence_second_award_cancel",
                    "${config.environment}_complaint_simple_defence_second_award_disqualification",
                    "${config.environment}_complaint_simple_defence_third_award_cancel",
                    "${config.environment}_complaint_simple_defence_third_award_disqualification",
                    "${config.environment}_criteria_patch_evidence",
                    "${config.environment}_aboveThresholdEU_LLC",
                    "${config.environment}_aboveThresholdUA_LLC",
                    "${config.environment}_simple_defence_LLC",
                    "${config.environment}_aboveThresholdUA_plans_aggregation",
                ]
                if (config.environment != 'k8s') {
                    innerJobs.addAll([
                        "${config.environment}_dasu_cancelled",
                        "${config.environment}_dasu_closed",
                        "${config.environment}_dasu_completed",
                        "${config.environment}_dasu_complete_proceed_num",
                        "${config.environment}_dasu_declined",
                        "${config.environment}_dasu_adressed_stopped",
                        "${config.environment}_dasu_declined_stopped",
                        "${config.environment}_dasu_active_stopped",
                        "${config.environment}_openeu_complaint_tender_resolved",
                        "${config.environment}_openeu_complaint_tender_mistaken",
                        "${config.environment}_openeu_complaint_tender_declined",
                        "${config.environment}_openeu_complaint_tender_stopped",
                        "${config.environment}_openeu_complaint_tender_invalid",
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
                        "${config.environment}_openeu_complaint_award_resolved",
                        "${config.environment}_openeu_complaint_award_mistaken",
                        "${config.environment}_openeu_complaint_award_declined",
                        "${config.environment}_openeu_complaint_award_stopped",
                        "${config.environment}_openeu_complaint_award_invalid",
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
        choiceParam('API_HOST_URL', ['http://api.${RELEASE_NAME}.k8s.prozorro.gov.ua',
        'https://lb-api-staging.prozorro.gov.ua',
        'https://lb-api-sandbox-2.prozorro.gov.ua'], 'my description')
        choiceParam('API_VERSION', ['2.5', '2.4',], 'my description')
        choiceParam('EDR_HOST_URL', ['https://lb-edr-staging.prozorro.gov.ua',
        'https://lb-edr-sandbox-2.prozorro.gov.ua'], 'my description')
        choiceParam('EDR_VERSION', ['1.0', '0'], 'my description')
        choiceParam('DS_HOST_URL', ['https://upload-docs-staging.prozorro.gov.ua', 'https://upload-docs-sandbox-2.prozorro.gov.ua', 'http://ds.k8s.prozorro.gov.ua'], 'my description')
        choiceParam('DS_REGEXP', ["^http?:\\/\\/ds\\.k8s\\.prozorro\\.gov\\.ua\\/get\\/([0-9A-Fa-f]{32})",
        "^https?:\\/\\/public-docs(?:-staging)?\\.prozorro\\.gov\\.ua\\/get\\/([0-9A-Fa-f]{32})",
        "^https?:\\/\\/public-docs(?:-sandbox-2)?\\.prozorro\\.gov\\.ua\\/get\\/([0-9A-Fa-f]{32})"], 'my description')
        choiceParam('PAYMENT_API', ['https://integration-staging.prozorro.gov.ua/liqpay',
        'https://integration-sandbox-2.prozorro.gov.ua/liqpay'], 'my description')
        choiceParam('PAYMENT_API_VERSION', ['v1'], 'my description')
        choiceParam('AUCTION_REGEXP', ["^http?:\\/\\/auctions\\.\${RELEASE_NAME}\\.k8s\\.prozorro\\.gov\\.ua\\/(esco-)?tenders\\/([0-9A-Fa-f]{32})",
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

     listView("complaints") {
        description('complaints for all procedure types')
        jobs {
            names("complaint",
                "complaint_aboveThresholdUA_award_invalid",
                "complaint_aboveThresholdUA_award_stopped",
                "complaint_aboveThresholdUA_award_declined",
                "complaint_aboveThresholdUA_award_mistaken",
                "complaint_aboveThresholdUA_award_resolved",
                "complaint_aboveThresholdUA_cancel_lot_resolved",
                "complaint_aboveThresholdUA_cancel_lot_mistaken",
                "complaint_aboveThresholdUA_cancel_lot_invalid",
                "complaint_aboveThresholdUA_cancel_lot_declined",
                "complaint_aboveThresholdUA_cancel_lot_stopped",
                "complaint_aboveThresholdUA_cancel_tender_resolved",
                "complaint_aboveThresholdUA_cancel_tender_mistaken",
                "complaint_aboveThresholdUA_cancel_tender_invalid",
                "complaint_aboveThresholdUA_cancel_tender_declined",
                "complaint_aboveThresholdUA_cancel_tender_stopped",
                "complaint_aboveThresholdUA_lot_resolved",
                "complaint_aboveThresholdUA_lot_mistaken",
                "complaint_aboveThresholdUA_lot_invalid",
                "complaint_aboveThresholdUA_lot_declined",
                "complaint_aboveThresholdUA_lot_stopped",
                "complaint_aboveThresholdUA_tender_resolved",
                "complaint_aboveThresholdUA_tender_mistaken",
                "complaint_aboveThresholdUA_tender_declined",
                "complaint_aboveThresholdUA_tender_stopped",
                "complaint_aboveThresholdUA_tender_invalid",
                "complaint_esco_tender_resolved",
                "complaint_esco_tender_mistaken",
                "complaint_esco_tender_declined",
                "complaint_esco_tender_stopped",
                "complaint_esco_tender_invalid",
                "complaint_esco_award_resolved",
                "complaint_esco_award_mistaken",
                "complaint_esco_award_declined",
                "complaint_esco_award_stopped",
                "complaint_esco_award_invalid",
                "complaint_esco_qualification_resolved",
                "complaint_esco_qualification_mistaken",
                "complaint_esco_qualification_invalid",
                "complaint_esco_qualification_declined",
                "complaint_esco_qualification_stopped",
                "complaint_esco_lot_resolved",
                "complaint_esco_lot_mistaken",
                "complaint_esco_lot_invalid",
                "complaint_esco_lot_declined",
                "complaint_esco_lot_stopped",
                "complaint_esco_cancel_tender_resolved",
                "complaint_esco_cancel_tender_mistaken",
                "complaint_esco_cancel_tender_invalid",
                "complaint_esco_cancel_tender_declined",
                "complaint_esco_cancel_tender_stopped",
                "complaint_esco_cancel_lot_resolved",
                "complaint_esco_cancel_lot_mistaken",
                "complaint_esco_cancel_lot_invalid",
                "complaint_esco_cancel_lot_declined",
                "complaint_esco_cancel_lot_stopped",
                "complaint_competitive_dialogue_eu_tender_resolved",
                "complaint_competitive_dialogue_eu_tender_mistaken",
                "complaint_competitive_dialogue_eu_tender_declined",
                "complaint_competitive_dialogue_eu_tender_stopped",
                "complaint_competitive_dialogue_eu_tender_invalid",
                "complaint_competitive_dialogue_eu_qualification_resolved",
                "complaint_competitive_dialogue_eu_qualification_mistaken",
                "complaint_competitive_dialogue_eu_qualification_invalid",
                "complaint_competitive_dialogue_eu_qualification_declined",
                "complaint_competitive_dialogue_eu_qualification_stopped",
                "complaint_competitive_dialogue_eu_lot_resolved",
                "complaint_competitive_dialogue_eu_lot_mistaken",
                "complaint_competitive_dialogue_eu_lot_invalid",
                "complaint_competitive_dialogue_eu_lot_declined",
                "complaint_competitive_dialogue_eu_lot_stopped",
                "complaint_competitive_dialogue_eu_cancel_tender_resolved",
                "complaint_competitive_dialogue_eu_cancel_tender_mistaken",
                "complaint_competitive_dialogue_eu_cancel_tender_invalid",
                "complaint_competitive_dialogue_eu_cancel_tender_declined",
                "complaint_competitive_dialogue_eu_cancel_tender_stopped",
                "complaint_competitive_dialogue_eu_cancel_lot_resolved",
                "complaint_competitive_dialogue_eu_cancel_lot_mistaken",
                "complaint_competitive_dialogue_eu_cancel_lot_invalid",
                "complaint_competitive_dialogue_eu_cancel_lot_declined",
                "complaint_competitive_dialogue_eu_cancel_lot_stopped",
                "complaint_competitive_dialogue_ua_tender_resolved",
                "complaint_competitive_dialogue_ua_tender_mistaken",
                "complaint_competitive_dialogue_ua_tender_declined",
                "complaint_competitive_dialogue_ua_tender_stopped",
                "complaint_competitive_dialogue_ua_tender_invalid",
                "complaint_competitive_dialogue_ua_qualification_resolved",
                "complaint_competitive_dialogue_ua_qualification_mistaken",
                "complaint_competitive_dialogue_ua_qualification_invalid",
                "complaint_competitive_dialogue_ua_qualification_declined",
                "complaint_competitive_dialogue_ua_qualification_stopped",
                "complaint_competitive_dialogue_ua_lot_resolved",
                "complaint_competitive_dialogue_ua_lot_mistaken",
                "complaint_competitive_dialogue_ua_lot_invalid",
                "complaint_competitive_dialogue_ua_lot_declined",
                "complaint_competitive_dialogue_ua_lot_stopped",
                "complaint_competitive_dialogue_ua_cancel_tender_resolved",
                "complaint_competitive_dialogue_ua_cancel_tender_mistaken",
                "complaint_competitive_dialogue_ua_cancel_tender_invalid",
                "complaint_competitive_dialogue_ua_cancel_tender_declined",
                "complaint_competitive_dialogue_ua_cancel_tender_stopped",
                "complaint_competitive_dialogue_ua_cancel_lot_resolved",
                "complaint_competitive_dialogue_ua_cancel_lot_mistaken",
                "complaint_competitive_dialogue_ua_cancel_lot_invalid",
                "complaint_competitive_dialogue_ua_cancel_lot_declined",
                "complaint_competitive_dialogue_ua_cancel_lot_stopped",
                "complaint_close_framework_agreement_ua_tender_resolved",
                "complaint_close_framework_agreement_ua_tender_mistaken",
                "complaint_close_framework_agreement_ua_tender_declined",
                "complaint_close_framework_agreement_ua_tender_stopped",
                "complaint_close_framework_agreement_ua_tender_invalid",
                "complaint_close_framework_agreement_ua_award_resolved",
                "complaint_close_framework_agreement_ua_award_mistaken",
                "complaint_close_framework_agreement_ua_award_declined",
                "complaint_close_framework_agreement_ua_award_stopped",
                "complaint_close_framework_agreement_ua_award_invalid",
                "complaint_close_framework_agreement_ua_qualification_resolved",
                "complaint_close_framework_agreement_ua_qualification_mistaken",
                "complaint_close_framework_agreement_ua_qualification_invalid",
                "complaint_close_framework_agreement_ua_qualification_declined",
                "complaint_close_framework_agreement_ua_qualification_stopped",
                "complaint_close_framework_agreement_ua_lot_resolved",
                "complaint_close_framework_agreement_ua_lot_mistaken",
                "complaint_close_framework_agreement_ua_lot_invalid",
                "complaint_close_framework_agreement_ua_lot_declined",
                "complaint_close_framework_agreement_ua_lot_stopped",
                "complaint_close_framework_agreement_ua_cancel_tender_resolved",
                "complaint_close_framework_agreement_ua_cancel_tender_mistaken",
                "complaint_close_framework_agreement_ua_cancel_tender_invalid",
                "complaint_close_framework_agreement_ua_cancel_tender_declined",
                "complaint_close_framework_agreement_ua_cancel_tender_stopped",
                "complaint_close_framework_agreement_ua_cancel_lot_resolved",
                "complaint_close_framework_agreement_ua_cancel_lot_mistaken",
                "complaint_close_framework_agreement_ua_cancel_lot_invalid",
                "complaint_close_framework_agreement_ua_cancel_lot_declined",
                "complaint_close_framework_agreement_ua_cancel_lot_stopped",
                "complaint_competitive_dialogue_eu_stage_2_tender_invalid",
                "complaint_competitive_dialogue_eu_stage_2_tender_stopped",
                "complaint_competitive_dialogue_eu_stage_2_tender_declined",
                "complaint_competitive_dialogue_eu_stage_2_tender_mistaken",
                "complaint_competitive_dialogue_eu_stage_2_tender_resolved",
                "complaint_competitive_dialogue_eu_stage_2_lot_resolved",
                "complaint_competitive_dialogue_eu_stage_2_lot_mistaken",
                "complaint_competitive_dialogue_eu_stage_2_lot_invalid",
                "complaint_competitive_dialogue_eu_stage_2_lot_declined",
                "complaint_competitive_dialogue_eu_stage_2_lot_stopped",
                "complaint_competitive_dialogue_eu_stage_2_cancel_tender_resolved",
                "complaint_competitive_dialogue_eu_stage_2_cancel_tender_mistaken",
                "complaint_competitive_dialogue_eu_stage_2_cancel_tender_invalid",
                "complaint_competitive_dialogue_eu_stage_2_cancel_tender_declined",
                "complaint_competitive_dialogue_eu_stage_2_cancel_tender_stopped",
                "complaint_competitive_dialogue_eu_stage_2_cancel_lot_resolved",
                "complaint_competitive_dialogue_eu_stage_2_cancel_lot_mistaken",
                "complaint_competitive_dialogue_eu_stage_2_cancel_lot_invalid",
                "complaint_competitive_dialogue_eu_stage_2_cancel_lot_declined",
                "complaint_competitive_dialogue_eu_stage_2_cancel_lot_stopped",
                "complaint_competitive_dialogue_ua_stage_2_tender_resolved",
                "complaint_competitive_dialogue_ua_stage_2_tender_mistaken",
                "complaint_competitive_dialogue_ua_stage_2_tender_declined",
                "complaint_competitive_dialogue_ua_stage_2_tender_stopped",
                "complaint_competitive_dialogue_ua_stage_2_tender_invalid",
                "complaint_competitive_dialogue_ua_stage_2_lot_resolved",
                "complaint_competitive_dialogue_ua_stage_2_lot_mistaken",
                "complaint_competitive_dialogue_ua_stage_2_lot_invalid",
                "complaint_competitive_dialogue_ua_stage_2_lot_declined",
                "complaint_competitive_dialogue_ua_stage_2_lot_stopped",
                "complaint_competitive_dialogue_ua_stage_2_cancel_tender_resolved",
                "complaint_competitive_dialogue_ua_stage_2_cancel_tender_mistaken",
                "complaint_competitive_dialogue_ua_stage_2_cancel_tender_invalid",
                "complaint_competitive_dialogue_ua_stage_2_cancel_tender_declined",
                "complaint_competitive_dialogue_ua_stage_2_cancel_tender_stopped",
                "complaint_competitive_dialogue_ua_stage_2_cancel_lot_resolved",
                "complaint_competitive_dialogue_ua_stage_2_cancel_lot_mistaken",
                "complaint_competitive_dialogue_ua_stage_2_cancel_lot_invalid",
                "complaint_competitive_dialogue_ua_stage_2_cancel_lot_declined",
                "complaint_competitive_dialogue_ua_stage_2_cancel_lot_stopped",
                "complaint_simple_defense_cancel_lot_resolved",
                "complaint_simple_defense_cancel_lot_mistaken",
                "complaint_simple_defense_cancel_lot_invalid",
                "complaint_simple_defense_cancel_lot_declined",
                "complaint_simple_defense_cancel_lot_stopped",
                "complaint_simple_defense_cancel_tender_resolved",
                "complaint_simple_defense_cancel_tender_mistaken",
                "complaint_simple_defense_cancel_tender_invalid",
                "complaint_simple_defense_cancel_tender_declined",
                "complaint_simple_defense_cancel_tender_stopped",
                "complaint_simple_defense_lot_resolved",
                "complaint_simple_defense_lot_mistaken",
                "complaint_simple_defense_lot_invalid",
                "complaint_simple_defense_lot_declined",
                "complaint_simple_defense_lot_stopped",
                "complaint_simple_defense_tender_resolved",
                "complaint_simple_defense_tender_mistaken",
                "complaint_simple_defense_tender_declined",
                "complaint_simple_defense_tender_stopped",
                "complaint_simple_defense_tender_invalid",
                "complaint_simple_defense_award_resolved",
                "complaint_simple_defense_award_mistaken",
                "complaint_simple_defense_award_declined",
                "complaint_simple_defense_award_stopped",
                "complaint_simple_defense_award_invalid",)
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

multiJob("complaint") {
    description('my description')
    parameters {
        choiceParam('BRANCH', ['master', 'dev_prozorro_2'], 'my description')
        stringParam('RELEASE_NAME', 'main', 'my description')
        choiceParam('API_HOST_URL', ['https://lb-api-staging.prozorro.gov.ua',
        'https://lb-api-sandbox-2.prozorro.gov.ua',
        'http://api.${RELEASE_NAME}.k8s.prozorro.gov.ua'], 'my description')
        choiceParam('API_VERSION', ['2.5'], 'my description')
        choiceParam('EDR_HOST_URL', ['https://lb-edr-staging.prozorro.gov.ua',
        'https://lb-edr-sandbox-2.prozorro.gov.ua'], 'my description')
        choiceParam('EDR_VERSION', ['1.0'], 'my description')
        choiceParam('DS_HOST_URL', ['https://upload-docs-staging.prozorro.gov.ua',
        'https://upload-docs-sandbox-2.prozorro.gov.ua',
        'http://ds.k8s.prozorro.gov.ua'], 'my description')
        choiceParam('DS_REGEXP', ["^https?:\\/\\/public-docs(?:-staging)?\\.prozorro\\.gov\\.ua\\/get\\/([0-9A-Fa-f]{32})",
        "^https?:\\/\\/public-docs(?:-sandbox-2)?\\.prozorro\\.gov\\.ua\\/get\\/([0-9A-Fa-f]{32})",
        "^http?:\\/\\/ds\\.k8s\\.prozorro\\.gov\\.ua\\/get\\/([0-9A-Fa-f]{32})"], 'my description')
        choiceParam('PAYMENT_API', ['https://integration-staging.prozorro.gov.ua/liqpay',
        'https://integration-sandbox-2.prozorro.gov.ua/liqpay'], 'my description')
        choiceParam('PAYMENT_API_VERSION', ['v1'], 'my description')
        choiceParam('AUCTION_REGEXP', ["^https?:\\/\\/auction(?:-staging)?\\.prozorro\\.gov\\.ua\\/(esco-)?tenders\\/([0-9A-Fa-f]{32})",
        "^https?:\\/\\/auction(?:-sandbox-2)?\\.prozorro\\.gov\\.ua\\/(esco-)?tenders\\/([0-9A-Fa-f]{32})",
        "^http?:\\/\\/auctions\\.\${RELEASE_NAME}\\.k8s\\.prozorro\\.gov\\.ua\\/(esco-)?tenders\\/([0-9A-Fa-f]{32})"], 'my description')
    }
        steps {
            phase("Test") {
                def innerJobs = [
                "complaint_aboveThresholdUA_award_invalid",
                "complaint_aboveThresholdUA_award_stopped",
                "complaint_aboveThresholdUA_award_declined",
                "complaint_aboveThresholdUA_award_mistaken",
                "complaint_aboveThresholdUA_award_resolved",
                "complaint_aboveThresholdUA_cancel_lot_resolved",
                "complaint_aboveThresholdUA_cancel_lot_mistaken",
                "complaint_aboveThresholdUA_cancel_lot_invalid",
                "complaint_aboveThresholdUA_cancel_lot_declined",
                "complaint_aboveThresholdUA_cancel_lot_stopped",
                "complaint_aboveThresholdUA_cancel_tender_resolved",
                "complaint_aboveThresholdUA_cancel_tender_mistaken",
                "complaint_aboveThresholdUA_cancel_tender_invalid",
                "complaint_aboveThresholdUA_cancel_tender_declined",
                "complaint_aboveThresholdUA_cancel_tender_stopped",
                "complaint_aboveThresholdUA_lot_resolved",
                "complaint_aboveThresholdUA_lot_mistaken",
                "complaint_aboveThresholdUA_lot_invalid",
                "complaint_aboveThresholdUA_lot_declined",
                "complaint_aboveThresholdUA_lot_stopped",
                "complaint_aboveThresholdUA_tender_resolved",
                "complaint_aboveThresholdUA_tender_mistaken",
                "complaint_aboveThresholdUA_tender_declined",
                "complaint_aboveThresholdUA_tender_stopped",
                "complaint_aboveThresholdUA_tender_invalid",
                "complaint_esco_tender_resolved",
                "complaint_esco_tender_mistaken",
                "complaint_esco_tender_declined",
                "complaint_esco_tender_stopped",
                "complaint_esco_tender_invalid",
                "complaint_esco_award_resolved",
                "complaint_esco_award_mistaken",
                "complaint_esco_award_declined",
                "complaint_esco_award_stopped",
                "complaint_esco_award_invalid",
                "complaint_esco_qualification_resolved",
                "complaint_esco_qualification_mistaken",
                "complaint_esco_qualification_invalid",
                "complaint_esco_qualification_declined",
                "complaint_esco_qualification_stopped",
                "complaint_esco_lot_resolved",
                "complaint_esco_lot_mistaken",
                "complaint_esco_lot_invalid",
                "complaint_esco_lot_declined",
                "complaint_esco_lot_stopped",
                "complaint_esco_cancel_tender_resolved",
                "complaint_esco_cancel_tender_mistaken",
                "complaint_esco_cancel_tender_invalid",
                "complaint_esco_cancel_tender_declined",
                "complaint_esco_cancel_tender_stopped",
                "complaint_esco_cancel_lot_resolved",
                "complaint_esco_cancel_lot_mistaken",
                "complaint_esco_cancel_lot_invalid",
                "complaint_esco_cancel_lot_declined",
                "complaint_esco_cancel_lot_stopped",
                "complaint_competitive_dialogue_eu_tender_resolved",
                "complaint_competitive_dialogue_eu_tender_mistaken",
                "complaint_competitive_dialogue_eu_tender_declined",
                "complaint_competitive_dialogue_eu_tender_stopped",
                "complaint_competitive_dialogue_eu_tender_invalid",
                "complaint_competitive_dialogue_eu_qualification_resolved",
                "complaint_competitive_dialogue_eu_qualification_mistaken",
                "complaint_competitive_dialogue_eu_qualification_invalid",
                "complaint_competitive_dialogue_eu_qualification_declined",
                "complaint_competitive_dialogue_eu_qualification_stopped",
                "complaint_competitive_dialogue_eu_lot_resolved",
                "complaint_competitive_dialogue_eu_lot_mistaken",
                "complaint_competitive_dialogue_eu_lot_invalid",
                "complaint_competitive_dialogue_eu_lot_declined",
                "complaint_competitive_dialogue_eu_lot_stopped",
                "complaint_competitive_dialogue_eu_cancel_tender_resolved",
                "complaint_competitive_dialogue_eu_cancel_tender_mistaken",
                "complaint_competitive_dialogue_eu_cancel_tender_invalid",
                "complaint_competitive_dialogue_eu_cancel_tender_declined",
                "complaint_competitive_dialogue_eu_cancel_tender_stopped",
                "complaint_competitive_dialogue_eu_cancel_lot_resolved",
                "complaint_competitive_dialogue_eu_cancel_lot_mistaken",
                "complaint_competitive_dialogue_eu_cancel_lot_invalid",
                "complaint_competitive_dialogue_eu_cancel_lot_declined",
                "complaint_competitive_dialogue_eu_cancel_lot_stopped",
                "complaint_competitive_dialogue_ua_tender_resolved",
                "complaint_competitive_dialogue_ua_tender_mistaken",
                "complaint_competitive_dialogue_ua_tender_declined",
                "complaint_competitive_dialogue_ua_tender_stopped",
                "complaint_competitive_dialogue_ua_tender_invalid",
                "complaint_competitive_dialogue_ua_qualification_resolved",
                "complaint_competitive_dialogue_ua_qualification_mistaken",
                "complaint_competitive_dialogue_ua_qualification_invalid",
                "complaint_competitive_dialogue_ua_qualification_declined",
                "complaint_competitive_dialogue_ua_qualification_stopped",
                "complaint_competitive_dialogue_ua_lot_resolved",
                "complaint_competitive_dialogue_ua_lot_mistaken",
                "complaint_competitive_dialogue_ua_lot_invalid",
                "complaint_competitive_dialogue_ua_lot_declined",
                "complaint_competitive_dialogue_ua_lot_stopped",
                "complaint_competitive_dialogue_ua_cancel_tender_resolved",
                "complaint_competitive_dialogue_ua_cancel_tender_mistaken",
                "complaint_competitive_dialogue_ua_cancel_tender_invalid",
                "complaint_competitive_dialogue_ua_cancel_tender_declined",
                "complaint_competitive_dialogue_ua_cancel_tender_stopped",
                "complaint_competitive_dialogue_ua_cancel_lot_resolved",
                "complaint_competitive_dialogue_ua_cancel_lot_mistaken",
                "complaint_competitive_dialogue_ua_cancel_lot_invalid",
                "complaint_competitive_dialogue_ua_cancel_lot_declined",
                "complaint_competitive_dialogue_ua_cancel_lot_stopped",
                "complaint_close_framework_agreement_ua_tender_resolved",
                "complaint_close_framework_agreement_ua_tender_mistaken",
                "complaint_close_framework_agreement_ua_tender_declined",
                "complaint_close_framework_agreement_ua_tender_stopped",
                "complaint_close_framework_agreement_ua_tender_invalid",
                "complaint_close_framework_agreement_ua_award_resolved",
                "complaint_close_framework_agreement_ua_award_mistaken",
                "complaint_close_framework_agreement_ua_award_declined",
                "complaint_close_framework_agreement_ua_award_stopped",
                "complaint_close_framework_agreement_ua_award_invalid",
                "complaint_close_framework_agreement_ua_qualification_resolved",
                "complaint_close_framework_agreement_ua_qualification_mistaken",
                "complaint_close_framework_agreement_ua_qualification_invalid",
                "complaint_close_framework_agreement_ua_qualification_declined",
                "complaint_close_framework_agreement_ua_qualification_stopped",
                "complaint_close_framework_agreement_ua_lot_resolved",
                "complaint_close_framework_agreement_ua_lot_mistaken",
                "complaint_close_framework_agreement_ua_lot_invalid",
                "complaint_close_framework_agreement_ua_lot_declined",
                "complaint_close_framework_agreement_ua_lot_stopped",
                "complaint_close_framework_agreement_ua_cancel_tender_resolved",
                "complaint_close_framework_agreement_ua_cancel_tender_mistaken",
                "complaint_close_framework_agreement_ua_cancel_tender_invalid",
                "complaint_close_framework_agreement_ua_cancel_tender_declined",
                "complaint_close_framework_agreement_ua_cancel_tender_stopped",
                "complaint_close_framework_agreement_ua_cancel_lot_resolved",
                "complaint_close_framework_agreement_ua_cancel_lot_mistaken",
                "complaint_close_framework_agreement_ua_cancel_lot_invalid",
                "complaint_close_framework_agreement_ua_cancel_lot_declined",
                "complaint_close_framework_agreement_ua_cancel_lot_stopped",
                "complaint_competitive_dialogue_eu_stage_2_tender_invalid",
                "complaint_competitive_dialogue_eu_stage_2_tender_stopped",
                "complaint_competitive_dialogue_eu_stage_2_tender_declined",
                "complaint_competitive_dialogue_eu_stage_2_tender_mistaken",
                "complaint_competitive_dialogue_eu_stage_2_tender_resolved",
                "complaint_competitive_dialogue_eu_stage_2_lot_resolved",
                "complaint_competitive_dialogue_eu_stage_2_lot_mistaken",
                "complaint_competitive_dialogue_eu_stage_2_lot_invalid",
                "complaint_competitive_dialogue_eu_stage_2_lot_declined",
                "complaint_competitive_dialogue_eu_stage_2_lot_stopped",
                "complaint_competitive_dialogue_eu_stage_2_cancel_tender_resolved",
                "complaint_competitive_dialogue_eu_stage_2_cancel_tender_mistaken",
                "complaint_competitive_dialogue_eu_stage_2_cancel_tender_invalid",
                "complaint_competitive_dialogue_eu_stage_2_cancel_tender_declined",
                "complaint_competitive_dialogue_eu_stage_2_cancel_tender_stopped",
                "complaint_competitive_dialogue_eu_stage_2_cancel_lot_resolved",
                "complaint_competitive_dialogue_eu_stage_2_cancel_lot_mistaken",
                "complaint_competitive_dialogue_eu_stage_2_cancel_lot_invalid",
                "complaint_competitive_dialogue_eu_stage_2_cancel_lot_declined",
                "complaint_competitive_dialogue_eu_stage_2_cancel_lot_stopped",
                "complaint_competitive_dialogue_ua_stage_2_tender_resolved",
                "complaint_competitive_dialogue_ua_stage_2_tender_mistaken",
                "complaint_competitive_dialogue_ua_stage_2_tender_declined",
                "complaint_competitive_dialogue_ua_stage_2_tender_stopped",
                "complaint_competitive_dialogue_ua_stage_2_tender_invalid",
                "complaint_competitive_dialogue_ua_stage_2_lot_resolved",
                "complaint_competitive_dialogue_ua_stage_2_lot_mistaken",
                "complaint_competitive_dialogue_ua_stage_2_lot_invalid",
                "complaint_competitive_dialogue_ua_stage_2_lot_declined",
                "complaint_competitive_dialogue_ua_stage_2_lot_stopped",
                "complaint_competitive_dialogue_ua_stage_2_cancel_tender_resolved",
                "complaint_competitive_dialogue_ua_stage_2_cancel_tender_mistaken",
                "complaint_competitive_dialogue_ua_stage_2_cancel_tender_invalid",
                "complaint_competitive_dialogue_ua_stage_2_cancel_tender_declined",
                "complaint_competitive_dialogue_ua_stage_2_cancel_tender_stopped",
                "complaint_competitive_dialogue_ua_stage_2_cancel_lot_resolved",
                "complaint_competitive_dialogue_ua_stage_2_cancel_lot_mistaken",
                "complaint_competitive_dialogue_ua_stage_2_cancel_lot_invalid",
                "complaint_competitive_dialogue_ua_stage_2_cancel_lot_declined",
                "complaint_competitive_dialogue_ua_stage_2_cancel_lot_stopped",
                "complaint_simple_defense_cancel_lot_resolved",
                "complaint_simple_defense_cancel_lot_mistaken",
                "complaint_simple_defense_cancel_lot_invalid",
                "complaint_simple_defense_cancel_lot_declined",
                "complaint_simple_defense_cancel_lot_stopped",
                "complaint_simple_defense_cancel_tender_resolved",
                "complaint_simple_defense_cancel_tender_mistaken",
                "complaint_simple_defense_cancel_tender_invalid",
                "complaint_simple_defense_cancel_tender_declined",
                "complaint_simple_defense_cancel_tender_stopped",
                "complaint_simple_defense_lot_resolved",
                "complaint_simple_defense_lot_mistaken",
                "complaint_simple_defense_lot_invalid",
                "complaint_simple_defense_lot_declined",
                "complaint_simple_defense_lot_stopped",
                "complaint_simple_defense_tender_resolved",
                "complaint_simple_defense_tender_mistaken",
                "complaint_simple_defense_tender_declined",
                "complaint_simple_defense_tender_stopped",
                "complaint_simple_defense_tender_invalid",
                "complaint_simple_defense_award_resolved",
                "complaint_simple_defense_award_mistaken",
                "complaint_simple_defense_award_declined",
                "complaint_simple_defense_award_stopped",
                "complaint_simple_defense_award_invalid",
                ]
                innerJobs.each { String scenario -> phaseJob(scenario) {
                    currentJobParameters(true)
                    abortAllJobs(false)
                }}
            }
        }
    }

    listView("claims") {
        description('description')
        jobs {
            names("claims",
                    "claims_aboveThresholdEU_draft_cancel",
                    "claims_aboveThresholdEU_draft_claim_answer_cancel",
                    "claims_aboveThresholdEU_draft_claim_answer_resolve",
                    "claims_aboveThresholdEU_draft_claim_cancel",
                    "claims_aboveThresholdUA_draft_cancel",
                    "claims_aboveThresholdUA_draft_claim_answer_cancel",
                    "claims_aboveThresholdUA_draft_claim_answer_resolve",
                    "claims_aboveThresholdUA_draft_claim_cancel",
                    "claims_competitiveDialogueEU_draft_cancel",
                    "claims_competitiveDialogueEU_draft_claim_answer_cancel",
                    "claims_competitiveDialogueEU_draft_claim_answer_resolve",
                    "claims_competitiveDialogueEU_draft_claim_cancel",
                    "claims_competitiveDialogueUA_draft_cancel",
                    "claims_competitiveDialogueUA_draft_claim_answer_cancel",
                    "claims_competitiveDialogueUA_draft_claim_answer_resolve",
                    "claims_competitiveDialogueUA_draft_claim_cancel",
                    "claims_esco_draft_cancel",
                    "claims_esco_draft_claim_answer_cancel",
                    "claims_esco_draft_claim_answer_resolve",
                    "claims_esco_draft_claim_cancel",
                    "claims_closeFrameworkAgreementUA_draft_cancel",
                    "claims_closeFrameworkAgreementUA_draft_claim_answer_cancel",
                    "claims_closeFrameworkAgreementUA_draft_claim_answer_resolve",
                    "claims_closeFrameworkAgreementUA_draft_claim_cancel",
                    "claims_simple_defense_draft_cancel",
                    "claims_simple_defense_draft_claim_answer_cancel",
                    "claims_simple_defense_draft_claim_answer_resolve",
                    "claims_simple_defense_draft_claim_cancel",
            )
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

    multiJob("claims") {
    description('my description')
    parameters {
        choiceParam('BRANCH', ['dev_prozorro_2', 'master'], 'my description')
        stringParam('RELEASE_NAME', 'main', 'my description')
        choiceParam('API_HOST_URL', [
        'https://lb-api-staging.prozorro.gov.ua',
        'https://lb-api-sandbox-2.prozorro.gov.ua',
        'http://api.${RELEASE_NAME}.k8s.prozorro.gov.ua'], 'my description')
        choiceParam('API_VERSION', ['2.5'], 'my description')
        choiceParam('EDR_HOST_URL', ['https://lb-edr-staging.prozorro.gov.ua',
        'https://lb-edr-sandbox-2.prozorro.gov.ua'], 'my description')
        choiceParam('EDR_VERSION', ['1.0'], 'my description')
        choiceParam('DS_HOST_URL', ['https://upload-docs-staging.prozorro.gov.ua', 'https://upload-docs-sandbox-2.prozorro.gov.ua', 'http://ds.k8s.prozorro.gov.ua'], 'my description')
        choiceParam('DS_REGEXP', [
        "^https?:\\/\\/public-docs(?:-staging)?\\.prozorro\\.gov\\.ua\\/get\\/([0-9A-Fa-f]{32})",
        "^https?:\\/\\/public-docs(?:-sandbox-2)?\\.prozorro\\.gov\\.ua\\/get\\/([0-9A-Fa-f]{32})",
        "^http?:\\/\\/ds\\.k8s\\.prozorro\\.gov\\.ua\\/get\\/([0-9A-Fa-f]{32})"], 'my description')
        choiceParam('PAYMENT_API', ['https://integration-staging.prozorro.gov.ua/liqpay',
        'https://integration-sandbox-2.prozorro.gov.ua/liqpay'], 'my description')
        choiceParam('PAYMENT_API_VERSION', ['v1'], 'my description')
        choiceParam('AUCTION_REGEXP', [
        "^https?:\\/\\/auction(?:-staging)?\\.prozorro\\.gov\\.ua\\/(esco-)?tenders\\/([0-9A-Fa-f]{32})",
        "^https?:\\/\\/auction(?:-sandbox-2)?\\.prozorro\\.gov\\.ua\\/(esco-)?tenders\\/([0-9A-Fa-f]{32})",
        "^http?:\\/\\/auctions\\.\${RELEASE_NAME}\\.k8s\\.prozorro\\.gov\\.ua\\/(esco-)?tenders\\/([0-9A-Fa-f]{32})"], 'my description')
    }
        steps {
            phase("Test") {
                def innerJobs = [
                    "claims_aboveThresholdEU_draft_cancel",
                    "claims_aboveThresholdEU_draft_claim_answer_cancel",
                    "claims_aboveThresholdEU_draft_claim_answer_resolve",
                    "claims_aboveThresholdEU_draft_claim_cancel",
                    "claims_aboveThresholdUA_draft_cancel",
                    "claims_aboveThresholdUA_draft_claim_answer_cancel",
                    "claims_aboveThresholdUA_draft_claim_answer_resolve",
                    "claims_aboveThresholdUA_draft_claim_cancel",
                    "claims_competitiveDialogueEU_draft_cancel",
                    "claims_competitiveDialogueEU_draft_claim_answer_cancel",
                    "claims_competitiveDialogueEU_draft_claim_answer_resolve",
                    "claims_competitiveDialogueEU_draft_claim_cancel",
                    "claims_competitiveDialogueUA_draft_cancel",
                    "claims_competitiveDialogueUA_draft_claim_answer_cancel",
                    "claims_competitiveDialogueUA_draft_claim_answer_resolve",
                    "claims_competitiveDialogueUA_draft_claim_cancel",
                    "claims_esco_draft_cancel",
                    "claims_esco_draft_claim_answer_cancel",
                    "claims_esco_draft_claim_answer_resolve",
                    "claims_esco_draft_claim_cancel",
                    "claims_closeFrameworkAgreementUA_draft_cancel",
                    "claims_closeFrameworkAgreementUA_draft_claim_answer_cancel",
                    "claims_closeFrameworkAgreementUA_draft_claim_answer_resolve",
                    "claims_closeFrameworkAgreementUA_draft_claim_cancel",
                    "claims_simple_defense_draft_cancel",
                    "claims_simple_defense_draft_claim_answer_cancel",
                    "claims_simple_defense_draft_claim_answer_resolve",
                    "claims_simple_defense_draft_claim_cancel",
                ]
                innerJobs.each { String scenario -> phaseJob(scenario) {
                    currentJobParameters(true)
                    abortAllJobs(false)
                }}
            }
        }
    }


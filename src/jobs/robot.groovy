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
            includePattern('*')
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
                    "AUCTION_REGEXP": "^http?:\\/\\/auctions\\.\${RELEASE_NAME}\\.k8s\\.prozorro\\.gov\\.ua\\/(esco-)?tenders\\/([0-9A-Fa-f]{32})",
                    "DS_REGEXP": "^http?:\\/\\/ds\\.k8s\\.prozorro\\.gov\\.ua\\/get\\/([0-9A-Fa-f]{32})",
                ],
                cron: null,
                branch: "dev_prozorro",
                concurrentBuild: true,
                edr: false,
        ],
        [
                environment: 'sandbox_openprocurement',
                params: [
                    "API_HOST_URL": "https://lb.api-sandbox.openprocurement.org",
                    "DS_HOST_URL": "https://upload.docs-sandbox.openprocurement.org",
                    "EDR_HOST_URL": "https://lb.edr-sandbox.openprocurement.org",
                    "API_VERSION": "2.4",
                    "EDR_VERSION": "0",
                    "AUCTION_REGEXP": "^https?:\\/\\/auction-sandbox\\.openprocurement\\.org\\/tenders\\/([0-9A-Fa-f]{32})",
                    "DS_REGEXP": "^https?:\\/\\/public\\.docs-sandbox\\.openprocurement\\.org\\/get\\/([0-9A-Fa-f]{32})",
                ],
                cron: null,
                branch: "master",
                concurrentBuild: false,
                edr: false
        ],
        [
                environment: 'sandbox_old_prozorro',
                params: [
                    "API_HOST_URL": "https://lb.api-sandbox.prozorro.gov.ua",
                    "DS_HOST_URL": "https://upload.docs-sandbox.prozorro.gov.ua",
                    "EDR_HOST_URL": "https://lb.edr-sandbox.prozorro.gov.ua",
                    "DASU_API_HOST_URL": "https://audit-api-sb.prozorro.gov.ua",
                    "API_VERSION": "2.4",
                    "EDR_VERSION": "0",
                    "AUCTION_REGEXP": "^https?:\\/\\/auction-sb\\.prozorro\\.gov\\.ua\\/tenders\\/([0-9A-Fa-f]{32})",
                    "DS_REGEXP": "^https?:\\/\\/public\\.docs-sandbox\\.prozorro\\.gov\\.ua\\/get\\/([0-9A-Fa-f]{32})",
                ],
                cron: null,
                branch: "master",
                concurrentBuild: false,
                edr: false
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
                branch: "master",
                concurrentBuild: false,
                edr: true
        ],
        [
                environment: 'gc_dev_prozorro',
                params: [
                    "API_HOST_URL": "https://api-dev-gc.prozorro.gov.ua",
                    "DS_HOST_URL": "https://upload-docs-dev-gc.prozorro.gov.ua",
                    "EDR_HOST_URL": "https://edr-dev-gc.prozorro.gov.ua",
                    "DASU_API_HOST_URL": "https://audit-api-dev-gc.prozorro.gov.ua",
                    "API_VERSION": "2.4",
                    "EDR_VERSION": "0",
                    "AUCTION_REGEXP": "^https?:\\/\\/auction(?:-dev-gc)?\\.prozorro\\.gov\\.ua\\/(esco-)?tenders\\/([0-9A-Fa-f]{32})",
                    "DS_REGEXP": "^https?:\\/\\/public-docs(?:-dev-gc)?\\.prozorro\\.gov\\.ua\\/get\\/([0-9A-Fa-f]{32})",
                ],
                cron: "H 2 * * *",
                branch: "dev_prozorro",
                concurrentBuild: false,
                edr: true
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
                    "AUCTION_REGEXP": "^https?:\\/\\/auction(?:-staging)?\\.prozorro\\.gov\\.ua\\/(esco-)?tenders\\/([0-9A-Fa-f]{32})",
                    "DS_REGEXP": "^https?:\\/\\/public-docs(?:-staging)?\\.prozorro\\.gov\\.ua\\/get\\/([0-9A-Fa-f]{32})",
                ],
                cron: null,
                branch: "master",
                concurrentBuild: false,
                edr: false
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
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION -v submissionMethodDetails:\"quick(mode:no-auction)\" $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION $params")
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

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper-o base_output.xml -s cancellation -A robot_tests_arguments/cancellation.txt -v MODE:openeu$params")
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
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION -v submissionMethodDetails:\"quick(mode:no-auction)\" $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION -v VAT_INCLUDED:False -v submissionMethodDetails:\"quick(mode:no-auction)\" $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION -v VAT_INCLUDED:False -v submissionMethodDetails:\"quick(mode:no-auction)\" $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION -v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"default\":{\"tender\":[0,31]}}}}' -v accelerator:2880 $params")
            shell("$robotWrapper $auction_short $defaultArgs -i auction $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell("$robotWrapper $agreement $defaultArgs $params")
            shell("$robotWrapper $selection -A robot_tests_arguments/framework_selection.txt $params")
            shell("$robotWrapper -o auction_short_framework_output.xml -s auction -A robot_tests_arguments/framework_selection.txt $params")
            shell("$robotWrapper -o qualification_framework_output.xml -s qualification -A robot_tests_arguments/framework_selection.txt $params")
            shell("$robotWrapper -o contract_framework_output.xml -s contract_signing -A robot_tests_arguments/framework_selection.txt $params")
            shell("$robotWrapper -o contract_management_framework_output.xml -s contract_management -A robot_tests_arguments/framework_selection.txt $params")
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

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper -o cancellation_output.xml -s cancellation -A robot_tests_arguments/cancellation.txt -v MODE:openua $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs -v submissionMethodDetails:\"quick(mode:no-auction)\" $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs -v submissionMethodDetails:\"quick(mode:no-auction)\" -v VAT_INCLUDED:False $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs -v VAT_INCLUDED:False -v submissionMethodDetails:\"quick(mode:no-auction)\" $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell("$robotWrapper $contractmanagement $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_belowThreshold_below_after_resolved_award_complaint") {
        parameters defaultParameters(config)
        description("Сценарій: Скарги на авард для допорогових закупівель.")
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
            shell("$robotWrapper -s complaints -A robot_tests_arguments/below_after_resolved_award_complaint.txt -v submissionMethodDetails:\"quick(mode:no-auction)\" $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_belowThreshold_below_before_resolved_award_complaint") {
        parameters defaultParameters(config)
        description("Сценарій: Скарги на авард для допорогових закупівель.")
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
            shell("$robotWrapper -s complaints -A robot_tests_arguments/below_before_resolved_award_complaint.txt -v submissionMethodDetails:\"quick(mode:no-auction)\" $params")
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

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper -o base_output.xml -s cancellation -A robot_tests_arguments/cancellation.txt $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_belowThreshold_complaints_tender_lot") {
        parameters defaultParameters(config)
        description("Сценарій: Скарги на умови закупівлі та лоту для допорогових закупівель.")
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
            shell("$robotWrapper -s complaints -A robot_tests_arguments/below_tender_lot_complaint.txt $params")
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

        String defaultArgs = "-A robot_tests_arguments/mnn_validation.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $openProcedure $defaultArgs $params")
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

        String defaultArgs = "-A robot_tests_arguments/competitive_dialogue_simple.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION $params")
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

        String defaultArgs = "-A robot_tests_arguments/competitive_dialogue_simple.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION -v submissionMethodDetails:\"quick(mode:no-auction)\" $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION $params")
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

        String defaultArgs = "-A robot_tests_arguments/competitive_dialogue_simple.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION -v submissionMethodDetails:\"quick(mode:no-auction)\" -v VAT_INCLUDED:False $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION $params")
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

        String defaultArgs = "-A robot_tests_arguments/competitive_dialogue_simple.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION -v submissionMethodDetails:\"quick(mode:no-auction)\" -v VAT_INCLUDED:False $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
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

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper -o cancellation_output.xml -s cancellation -A robot_tests_arguments/cancellation.txt -v MODE:open_competitive_dialogue $params")
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

        String defaultArgs = "-A robot_tests_arguments/competitive_dialogue_simple_UA.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION -v submissionMethodDetails:\"quick(mode:no-auction)\" $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION $params")
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

        String defaultArgs = "-A robot_tests_arguments/competitive_dialogue_simple_UA.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION -v submissionMethodDetails:\"quick(mode:no-auction)\" -v VAT_INCLUDED:False $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION $params")
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

        String defaultArgs = "-A robot_tests_arguments/competitive_dialogue_simple_UA.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION -v submissionMethodDetails:\"quick(mode:no-auction)\" -v VAT_INCLUDED:False $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION $params")
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
                shell("$robotWrapper -s $scenario $params")
                shell(shellRebot)
            }
        }

        job("${config.environment}_${scenario}_cancellation") {
            parameters defaultParameters(config)
            description("Сценарій: Скасування закупівлі ($scenario)")
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
                shell("$robotWrapper -o cancellation_output.xml -s cancellation -A robot_tests_arguments/limited_cancellation.txt -v MODE:$scenario $params")
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
            shell("$robotWrapper -o planning_output.xml -s planning -e closeframework_period -v NUMBER_OF_ITEMS:2 -v TENDER_MEAT:False -v ITEM_MEAT:False $params")
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
            shell("$robotWrapper -o planning_output.xml -s planning -v MODE:closeFrameworkAgreementUA -v NUMBER_OF_ITEMS:2 -v TENDER_MEAT:False -v ITEM_MEAT:False $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs -i answer_question_to_tender $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION $params")
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
            shell("$robotWrapper $openProcedure $defaultArgs \$EDR_PRE_QUALIFICATION $params")
            shell("$robotWrapper $auction_short $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs \$EDR_QUALIFICATION $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
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
                    "${config.environment}_aboveThresholdUA_cancellation",
                    "${config.environment}_aboveThresholdUA_vat_true_false",
                    "${config.environment}_aboveThresholdUA_vat_false_false",
                    "${config.environment}_aboveThresholdUA_vat_false_true",
                    "${config.environment}_below_funders_full",
                    "${config.environment}_belowThreshold",
                    "${config.environment}_belowThreshold_VAT_False",
                    "${config.environment}_belowThreshold_below_after_resolved_award_complaint",
                    "${config.environment}_belowThreshold_below_before_resolved_award_complaint",
                    "${config.environment}_belowThreshold_cancellation",
                    "${config.environment}_belowThreshold_complaints_tender_lot",
                    "${config.environment}_belowThreshold_moz",
                    "${config.environment}_belowThreshold_vat_true_false",
                    "${config.environment}_belowThreshold_vat_false_false",
                    "${config.environment}_belowThreshold_vat_false_true",
                    "${config.environment}_competitiveDialogueEU",
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
                    "${config.environment}_planning_belowThreshold",
                    "${config.environment}_planning_framework_agreement",
                    "${config.environment}_feed_reading",
                    "${config.environment}_single_item_tender",
                    "${config.environment}_aboveThresholdUA_defence_one_bid",
                    "${config.environment}_esco",
                    "${config.environment}_belowThreshold_moz_1",
                    "${config.environment}_belowThreshold_moz_2",
                    "${config.environment}_belowThreshold_moz_3",
                    "${config.environment}_belowThreshold_moz_validation",

                ]
                if (config.environment != 'k8s') {
                    innerJobs.addAll([
                        "${config.environment}_dasu_cancelled",
                        "${config.environment}_dasu_closed",
                        "${config.environment}_dasu_completed",
                        "${config.environment}_dasu_stopped",
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

def defaultConfigure = {
    it / 'properties' / 'jenkins.model.BuildDiscarderProperty' {
        strategy {
            'daysToKeep'('14')
            'numToKeep'('48')
            'artifactDaysToKeep'('-1')
            'artifactNumToKeep'('-1')
        }
    }
    it / 'properties' / 'com.coravy.hudson.plugins.github.GithubProjectProperty' {
        'projectUrl'('https://github.com/openprocurement/robot_tests/')
        displayName()
    }
}

def defaultScm = {
    git {
        remote {
            github("openprocurement/robot_tests", "https")
        }
        branch("*/master")ds
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
}


String shellBuildout = "python2 bootstrap.py\nbin/buildout -N buildout:always-checkout=force\nbin/develop update -f"
String shellPhantom  = "sed -r -i 's/browser: *(chrome|firefox)/browser:  PhantomJS/gi' op_robot_tests/tests_files/data/users.yaml"
String shellRebot    = "robot_wrapper bin/rebot -o test_output/output.xml -l test_output/log.html -r test_output/report.html -R test_output/*.xml"
String robotWrapper  = "robot_wrapper bin/op_tests --consolecolors on "
String openProcedure = "-o base_output.xml -s openProcedure"
String auction       = "-o auction_output.xml -s auction"
String qualification = "-o qualification_output.xml -s qualification"
String contractsign  = "-o contract_output.xml -s contract_signing"

[
        [
                environment: 'dsl_old',
                params: [
                    "API_HOST_URL": "https://lb.api-sandbox.openprocurement.org",
                    "DS_HOST_URL": "https://upload.docs-sandbox.openprocurement.org",
                    "EDR_HOST_URL": "https://lb.edr-sandbox.openprocurement.org",
                    "API_VERSION": "2.4",
                    "EDR_VERSION": "0",
                ]
        ],
//        [
//                environment: 'dsl_new',
//                params: [
//                        "API_HOST_URL": "https://lb-api-sandbox.prozorro.gov.ua",
//                        "DS_HOST_URL": "https://upload-docs-sandbox.prozorro.gov.ua",
//                        "EDR_HOST_URL": "https://lb-edr-sandbox.prozorro.gov.ua",
//                        "API_VERSION": "2.4",
//                        "EDR_VERSION": "0",
//                ]
//        ],
].each { Map config ->
    String params = config.params.collect { k,v -> " -v $k=$v" }.join('')

    job("${config.environment}_aboveThresholdEU") {
        description("Сценарій: Надпорогова закупівля з публікацією англійською мовою.")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(false)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure

        String defaultArgs = "-A robot_tests_arguments/openeu.txt -e add_doc_to_contract"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $openProcedure $defaultArgs -e resolve_tender_claim -e resolve_lot_claim$params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_aboveThresholdEU_cancellation") {
        description("Сценарій: Надпорогова закупівля з публікацією англійською мовою. Скасування закупівлі.")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(false)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper-o base_output.xml -s cancellation -A robot_tests_arguments/cancellation.txt -v MODE:openeu$params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_aboveThresholdEU_simple") {
        description("Сценарій: Відкрита процедура (короткий сценарій)")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(false)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure

        String defaultArgs = "-A robot_tests_arguments/openeu_simple.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $openProcedure $defaultArgs $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_aboveThresholdUA") {
        description("Сценарій: Надпорогова закупівля.")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(false)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure

        String defaultArgs = "-A robot_tests_arguments/openua.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $openProcedure $defaultArgs -e resolve_tender_claim -e resolve_tender_claim -e resolve_lot_claim -e add_doc_to_contract$params")
            shell("$robotWrapper $auction $defaultArgs -e add_doc_to_contract$params")
            shell("$robotWrapper $qualification $defaultArgs -e add_doc_to_contract$params")
            shell("$robotWrapper $contractsign $defaultArgs -e add_doc_to_contract$params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_aboveThresholdUA_cancellation") {
        description("Сценарій: Скасування закупівлі (openUA)")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(false)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper -o cancellation_output.xml -s cancellation -A robot_tests_arguments/cancellation.txt -v MODE:openua$params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_aboveThresholdUA_simple") {
        description("Сценарій: Відкрита процедура (короткий)")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(false)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure

        String defaultArgs = "-A robot_tests_arguments/openua_simple.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $openProcedure $defaultArgs $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_below_funders_full") {
        description("Сценарій: Допорогова закупівля.")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(false)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure

        String defaultArgs = "-A robot_tests_arguments/below_funders_full.txt -e answer_tender_claim -e answer_lot_claim -e answer_award_claim -e add_doc_to_contract"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $openProcedure $defaultArgs $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_belowThreshold") {
        description("Сценарій: Допорогова закупівля.")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(false)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure

        String defaultArgs = "-A robot_tests_arguments/below.txt -e answer_tender_claim -e answer_lot_claim -e answer_award_claim -e add_doc_to_contract"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $openProcedure $defaultArgs $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_belowThreshold_below_after_resolved_award_complaint") {
        description("Сценарій: Скарги на авард для допорогових закупівель.")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(false)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure

        steps {
            shell(shellBuildout)
            shell("$robotWrapper -s complaints -A robot_tests_arguments/below_after_resolved_award_complaint.txt -v submissionMethodDetails:\"quick(mode:no-auction)\" $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_belowThreshold_below_before_resolved_award_complaint") {
        description("Сценарій: Скарги на авард для допорогових закупівель.")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(false)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure

        steps {
            shell(shellBuildout)
            shell("$robotWrapper -s complaints -A robot_tests_arguments/below_before_resolved_award_complaint.txt -v submissionMethodDetails:\"quick(mode:no-auction)\" $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_belowThreshold_cancellation") {
        description("Сценарій: Скасування закупівлі (belowThreshold/ Допорогова закупівля)")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(false)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper -o base_output.xml -s cancellation -A robot_tests_arguments/cancellation.txt $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_belowThreshold_complaints_tender_lot") {
        description("Сценарій: Скарги на умови закупівлі та лоту для допорогових закупівель.")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(false)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper -s complaints -A robot_tests_arguments/below_tender_lot_complaint.txt $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_belowThreshold_moz") {
        description("Сценарій: Допорогова закупівля.")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(false)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure

        String defaultArgs = "-A robot_tests_arguments/below.txt -e answer_tender_claim -e answer_lot_claim -e answer_award_claim -e add_doc_to_contract"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $openProcedure $defaultArgs -v MOZ_INTEGRATION:True $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_belowThreshold_simple") {
        description("Допорогова закупівля (мінімальний набір тест кейсів)")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(false)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure

        String defaultArgs = "-A robot_tests_arguments/below_simple.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $openProcedure $defaultArgs $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_competitiveDialogueEU") {
        description("Сценарій: Конкурентний діалог з публікацією англійською мовою")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(false)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)

        configure defaultConfigure

        String defaultArgs = "-A robot_tests_arguments/competitive_dialogue_simple.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $openProcedure $defaultArgs $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_competitiveDialogueEU_stage1") {
        description("Сценарій: Конкурентний діалог з публікацією англійською мовою")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(false)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false, 10800)
        configure defaultConfigure

        String defaultArgs = "-A robot_tests_arguments/competitive_dialogue.txt -e resolve_tender_claim -e resolve_lot_claim"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $openProcedure $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_competitiveDialogueUA") {
        description("Сценарій: Конкурентний діалог для надпорогових закупівель українською мовою")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(false)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false, 10800)
        configure defaultConfigure

        String defaultArgs = "-A robot_tests_arguments/competitive_dialogue_UA.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $openProcedure $defaultArgs $params")
            shell("$robotWrapper $auction $defaultArgs $params")
            shell("$robotWrapper $qualification $defaultArgs $params")
            shell("$robotWrapper $contractsign $defaultArgs $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_competitiveDialogueUA_cancellation") {
        description("Сценарій: Скасування закупівлі (competitive dialogue)")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(false)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(false)
        configure defaultConfigure


        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper -o cancellation_output.xml -s cancellation -A robot_tests_arguments/cancellation.txt -v MODE:open_competitive_dialogue $params")
            shell(shellRebot)
        }
    }

    ["cancelled", "closed", "completed", "stopped"].each { String scenario ->
        job("${config.environment}_dasu_$scenario") {
            description("Сценарій: ДАСУ")
            keepDependencies(false)
            disabled(false)
            concurrentBuild(false)
            scm defaultScm
            publishers defaultPublishers
            wrappers defaultWrappers(true)
            configure defaultConfigure

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
            description("Сценарій: Переговорна процедура")
            keepDependencies(false)
            disabled(false)
            concurrentBuild(false)
            scm defaultScm
            publishers defaultPublishers
            wrappers defaultWrappers(scenario != 'negotiation.quick', 10800)
            configure defaultConfigure

            steps {
                shell(shellBuildout)
                shell(shellPhantom)
                shell("$robotWrapper -s $scenario $params")
                shell(shellRebot)
            }
        }

        job("${config.environment}_${scenario}_cancellation") {
            description("Сценарій: Скасування закупівлі ($scenario)")
            keepDependencies(false)
            disabled(false)
            concurrentBuild(false)
            scm defaultScm
            publishers defaultPublishers
            wrappers defaultWrappers(false)
            configure defaultConfigure

            steps {
                shell(shellBuildout)
                shell(shellPhantom)
                shell("$robotWrapper -o cancellation_output.xml -s cancellation -A robot_tests_arguments/limited_cancellation.txt -v MODE:$scenario $params")
                shell(shellRebot)
            }
        }
    }

    job("${config.environment}_planning") {
        description("Сценарій: Планування")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(false)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true)
        configure defaultConfigure

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper -o planning_output.xml -s planning -v NUMBER_OF_ITEMS:2 -v TENDER_MEAT:False -v ITEM_MEAT:False $params")
            shell(shellRebot)
        }
    }

    job("${config.environment}_single_item_tender") {
        description("Сценарій: Допорогова закупівля з однією номенклатурою до аукціону")
        keepDependencies(false)
        disabled(false)
        concurrentBuild(false)
        scm defaultScm
        publishers defaultPublishers
        wrappers defaultWrappers(true, 10800)
        configure defaultConfigure

        String defaultArgs = "-A robot_tests_arguments/single_item_tender.txt"

        steps {
            shell(shellBuildout)
            shell(shellPhantom)
            shell("$robotWrapper $openProcedure $defaultArgs $params")
            shell("$robotWrapper $auction $defaultArgs -e add_doc_to_contract $params")
            shell("$robotWrapper $qualification $defaultArgs -e add_doc_to_contract $params")
            shell("$robotWrapper $contractsign $defaultArgs -e add_doc_to_contract $params")
            shell(shellRebot)
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

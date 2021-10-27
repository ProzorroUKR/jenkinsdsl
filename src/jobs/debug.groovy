    job("competitive_dialogue_eu_complaint_tender_resolved") {
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
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:open_competitive_dialogue"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("competitive_dialogue_eu_complaint_tender_mistaken") {
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
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:open_competitive_dialogue"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("competitive_dialogue_eu_complaint_tender_declined") {
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
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:open_competitive_dialogue"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("competitive_dialogue_eu_complaint_tender_stopped") {
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
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:open_competitive_dialogue"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints  $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("competitive_dialogue_eu_complaint_tender_invalid") {
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
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:open_competitive_dialogue"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints  $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("competitive_dialogue_eu_complaint_qualification_resolved") {
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
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:open_competitive_dialogue"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1 -i make_bid_with_criteria_by_provider_first_stage -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("competitive_dialogue_eu_complaint_qualification_mistaken") {
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
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:open_competitive_dialogue"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1 -i make_bid_with_criteria_by_provider_first_stage -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("competitive_dialogue_eu_complaint_qualification_invalid") {
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
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:open_competitive_dialogue"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1 -i make_bid_with_criteria_by_provider_first_stage -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("competitive_dialogue_eu_complaint_qualification_declined") {
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
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:open_competitive_dialogue"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1 -i make_bid_with_criteria_by_provider_first_stage -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("competitive_dialogue_eu_complaint_qualification_stopped") {
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
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:open_competitive_dialogue"
        String bidding = "-e make_bid_with_criteria_by_provider -e make_bid_with_criteria_by_provider1 -i make_bid_with_criteria_by_provider_first_stage -i make_bid_with_criteria_by_provider1_first_stage -i make_bid_with_criteria_by_provider2_first_stage"
        String pre_qualification = "-i pre-qualification_approve_first_bid -i pre-qualification_approve_second_bid -i pre-qualification_approve_third_bid"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $bidding $pre_qualification $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("competitive_dialogue_eu_complaint_lot_resolved") {
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
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:open_competitive_dialogue"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("competitive_dialogue_eu_complaint_lot_mistaken") {
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
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:open_competitive_dialogue"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("competitive_dialogue_eu_complaint_lot_invalid") {
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
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:open_competitive_dialogue"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("competitive_dialogue_eu_complaint_lot_declined") {
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
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:open_competitive_dialogue"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("competitive_dialogue_eu_complaint_lot_stopped") {
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
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:open_competitive_dialogue"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("competitive_dialogue_eu_complaint_cancel_tender_resolved") {
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
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:open_competitive_dialogue"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("competitive_dialogue_eu_complaint_cancel_tender_mistaken") {
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
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:open_competitive_dialogue"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("competitive_dialogue_eu_complaint_cancel_tender_invalid") {
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
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:open_competitive_dialogue"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("competitive_dialogue_eu_complaint_cancel_tender_declined") {
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
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:open_competitive_dialogue"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("competitive_dialogue_eu_complaint_cancel_tender_stopped") {
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
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:open_competitive_dialogue"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("competitive_dialogue_eu_complaint_cancel_lot_resolved") {
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
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:open_competitive_dialogue"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("competitive_dialogue_eu_complaint_cancel_lot_mistaken") {
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
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:open_competitive_dialogue"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("competitive_dialogue_eu_complaint_cancel_lot_invalid") {
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
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:open_competitive_dialogue"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("competitive_dialogue_eu_complaint_cancel_lot_declined") {
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
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:open_competitive_dialogue"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }

    job("competitive_dialogue_eu_complaint_cancel_lot_stopped") {
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
        String accelerate_competitive_dialogue_eu = "-v 'BROKERS_PARAMS:{\"Quinta\":{\"intervals\":{\"open_competitive_dialogue\":{\"tender\":[1,5],\"accelerator\":8640}}}}'"
        String mode = "-v MODE:open_competitive_dialogue"

        steps {
            shell(shellBuildout)
            shell("$robotWrapper $planning -i create_plan -i find_plan -v MODE:competitiveDialogueEU $params")
            shell("$robotWrapper $complaints $defaultArgs $mode $accelerate_competitive_dialogue_eu $params")
            shell(shellRebot)
        }
    }
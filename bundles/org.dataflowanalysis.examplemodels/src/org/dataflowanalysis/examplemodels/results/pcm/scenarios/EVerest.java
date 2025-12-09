package org.dataflowanalysis.examplemodels.results.pcm.scenarios;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.examplemodels.results.ExpectedCharacteristic;
import org.dataflowanalysis.examplemodels.results.ExpectedViolation;
import org.dataflowanalysis.examplemodels.results.pcm.PCMExampleModelResult;
import org.dataflowanalysis.examplemodels.results.pcm.PCMIdentifier;

public class EVerest implements PCMExampleModelResult {
    @Override
    public String getBaseFolderName() {
        return "scenarios";
    }

    @Override
    public String getModelName() {
        return "EVerest";
    }

    /**
     * Builds an AnalysisConstraint item that can check if data with data status labels and without data status labels flow
     * to specific locations
     * @param withStatus, data with status label(s)
     * @param withoutStatus, data without status label(s)
     * @param locations, where data with the specified labels and not labels is not supposed to flow to
     * @return the AnalysisConstraint item that can be used for the Data Flow Analysis
     */
    public static AnalysisConstraint withStatusWithoutStatusNeverFlowsTo(List<String> withStatus, List<String> withoutStatus,
            List<String> locations) {
        var constraint = new ConstraintDSL().ofData()
                .withLabel("Status", withStatus)
                .withoutLabel("Status", withoutStatus)
                .fromNode()
                .neverFlows()
                .toVertex()
                .withCharacteristic("Location", locations)
                .create();
        return constraint;
    }

    /**
     * Builds an AnalysisConstraint item that checks if data with status labels and without status labels flows to specific
     * components
     * @param withStatus, the data status labels that data is supposed to have
     * @param withoutStatus, the data status labels that data is not supposed to have
     * @param components, where the data is not supposed to flow to
     * @return the AnalysisConstraint item
     */
    public static AnalysisConstraint withStatusWithoutStatusNeverFlowsToComponent(List<String> withStatus, List<String> withoutStatus,
            List<String> components) {
        var constraint = new ConstraintDSL().ofData()
                .withLabel("Status", withStatus)
                .withoutLabel("Status", withoutStatus)
                .fromNode()
                .neverFlows()
                .toVertex()
                .withCharacteristic("Component", components)
                .create();
        return constraint;
    }

    /**
     * Builds an AnalysisConstraint item that checks if data status labels are always set together
     * @param ifStatus, the first data status label
     * @param thenStatus, the second data status label
     * @return the AnalysisConstraint item that can be used for the data flow analysis
     */
    public static AnalysisConstraint ifStatusThenStatus(List<String> ifStatus, List<String> thenStatus) {
        var constraint = new ConstraintDSL().ofData()
                .withLabel("Status", ifStatus)
                .withoutLabel("Status", thenStatus)
                .fromNode()
                .neverFlows()
                .toVertex()
                .create();
        return constraint;
    }

    /**
     * Builds an AnalysisConstraint item that checks if data status labels never flow to specified locations
     * @param status, data labels of type status
     * @param locations, where the data with the specified labels is not supposed to flow to
     * @return the AnalysisConstraint item that can be used for the data flow analysis
     */
    public static AnalysisConstraint neverFlowsTo(List<String> status, List<String> locations) {
        var constraint = new ConstraintDSL().ofData()
                .withLabel("Status", status)
                .fromNode()
                .neverFlows()
                .toVertex()
                .withCharacteristic("Location", locations)
                .create();
        return constraint;
    }

    /**
     * Builds an AnalysisConstraint item that checks if data with specific data status labels never flow to specified
     * components
     * @param status, data labels of type status
     * @param components, where the specified data is not supposed to flow to
     * @return the AnalysisConstraint item that can be used for the data flow analysis
     */
    public static AnalysisConstraint neverFlowsToComponent(List<String> status, List<String> components) {
        var constraint = new ConstraintDSL().ofData()
                .withLabel("Status", status)
                .fromNode()
                .neverFlows()
                .toVertex()
                .withCharacteristic("Component", components)
                .create();
        return constraint;
    }

    /**
     * Builds an AnalysisConstraint item that ensures data with specific status labels can only flow to specified locations.
     * @param status the list of status labels representing the data type
     * @param locations the list of locations where the specified data is allowed to flow
     * @return the AnalysisConstraint item that can be used for the data flow analysis
     */
    public static AnalysisConstraint onlyFlowsTo(List<String> status, List<String> locations) {
        var constraint = new ConstraintDSL().ofData()
                .withLabel("Status", status)
                .fromNode()
                .neverFlows()
                .toVertex()
                .withoutCharacteristic("Location", locations)
                .create();
        return constraint;
    }

    /**
     * Builds an AnalysisConstraint item that ensures only specified actors have access to data with specific status labels.
     * @param actor the list of actors who are allowed to access the data
     * @param status the list of status labels representing the data type
     * @return the AnalysisConstraint item that can be used for the data access analysis
     */
    public static AnalysisConstraint onlyActorHasAccessToStatus(List<String> actor, List<String> status) {
        var constraint = new ConstraintDSL().ofData()
                .withLabel("Status", status)
                .fromNode()
                .neverFlows()
                .toVertex()
                .withoutCharacteristic("Actor", actor)
                .create();
        return constraint;
    }

    /**
     * Builds an AnalysisConstraint item that ensures only actors with specific status labels have access to certain
     * components.
     * @param actorStatus the list of actor status labels representing the allowed actors
     * @param components the list of components to which the specified actors with the given status are allowed access
     * @return the AnalysisConstraint item that can be used for the data flow and access analysis
     */
    public static AnalysisConstraint onlyActorWithStatusHasAccessToComponent(List<String> actorStatus, List<String> components) {
        var constraint = new ConstraintDSL().ofData()
                .withoutLabel("ActorStatus", actorStatus)
                .fromNode()
                .neverFlows()
                .toVertex()
                .withCharacteristic("Component", components)
                .create();
        return constraint;
    }

    /**
     * Builds an AnalysisConstraint item that ensures only actors with specific status labels have access to certain
     * locations.
     * @param actorStatus the list of actor status labels representing the allowed actors
     * @param locations the list of locations to which the specified actors with the given status are allowed access
     * @return the AnalysisConstraint item that can be used for the data flow and access analysis
     */

    public static AnalysisConstraint onlyActorWithStatusHasAccessToLocation(List<String> actorStatus, List<String> locations) {
        var constraint = new ConstraintDSL().ofData()
                .withoutLabel("ActorStatus", actorStatus)
                .fromNode()
                .neverFlows()
                .toVertex()
                .withCharacteristic("Location", locations)
                .create();
        return constraint;
    }

    /**
     * Builds an AnalysisConstraint item that ensures only actors with specific status labels have access to data with
     * certain status labels, while excluding other status labels.
     * @param actorStatus the list of actor status labels representing the allowed actors
     * @param withStatus the list of status labels representing the data the actors are allowed to access
     * @param withoutStatus the list of status labels representing the data the actors are not allowed to access
     * @return the AnalysisConstraint item that can be used for the data flow and access analysis
     */
    public static AnalysisConstraint onlyActorWithStatusHasAccessToDataWithStatusWithoutStatus(List<String> actorStatus, List<String> withStatus,
            List<String> withoutStatus) {
        var constraint = new ConstraintDSL().ofData()
                .withoutLabel("ActorStatus", actorStatus)
                .withLabel("Status", withStatus)
                .withoutLabel("Status", withoutStatus)
                .fromNode()
                .neverFlows()
                .toVertex()
                .create();
        return constraint;
    }

    @Override
    public List<AnalysisConstraint> getDSLConstraints() {
        List<AnalysisConstraint> constraints = new ArrayList<>();

        // Requirement #6:
        // Tokens used for authentication should not be stored in plain text in log files or persistent storage.
        // Authentication tokens should not leak to the outside
        constraints.add(
                withStatusWithoutStatusNeverFlowsTo(List.of("Token"), List.of("Hashed", "Encrypted"), List.of("TPM", "LocalStorage", "External")));
        constraints.add(withStatusWithoutStatusNeverFlowsToComponent(List.of("Token"), List.of("Hashed, Encrypted"), List.of("Logs")));

        // Requirement #25:
        // The OCPP modules shall store only the hash of the authorization token of an EV user persistently
        constraints.add(withStatusWithoutStatusNeverFlowsTo(List.of("Token"), List.of("Hashed"), List.of("LocalStorage", "TPM")));
        constraints.add(withStatusWithoutStatusNeverFlowsToComponent(List.of("Token"), List.of("Hashed"), List.of("Logs")));

        // Requirement #26:
        // The OCPP modules and the authorization related modules (Auth, TokenProvider, TokenValidator) shall ensure that no
        // plaintext authorization tokens are logged.
        constraints.add(withStatusWithoutStatusNeverFlowsToComponent(List.of("Token"), List.of("Hashed, Encrypted"), List.of("Logs")));

        // Requirement #30:
        // It shall be ensured that the API module that exposes the MQTT interface is used only by authorized external
        // components.
        constraints.add(onlyActorWithStatusHasAccessToComponent(List.of("Authorized"), List.of("API")));

        // Requirement #32:
        // The security certs should be stored securely in a hardware module that doesn't give access to the private keys.
        constraints.add(withStatusWithoutStatusNeverFlowsTo(List.of("Certificate"), List.of("Encrypted"), List.of("TPM")));
        constraints.add(neverFlowsTo(List.of("Certificate"), List.of("LocalStorage", "External")));
        constraints.add(neverFlowsToComponent(List.of("Certificate"), List.of("Logs")));

        // Requirement #37:
        // Authentication tokens & payment information should be provided to the relevant parties (CSMS, payment provider) and
        // everest needs to make sure that this information is not being made available otherwise (eg. in log files).
        constraints.add(onlyFlowsTo(List.of("Token"), List.of("CSMS", "PaymentProvider", "ChargingStation")));
        constraints.add(onlyFlowsTo(List.of("PaymentInformation"), List.of("CSMS", "PaymentProvider", "ChargingStation")));
        constraints.add(neverFlowsToComponent(List.of("Token"), List.of("Logs")));
        constraints.add(neverFlowsToComponent(List.of("PaymentInformation"), List.of("Logs")));

        // Requirement #38:
        // Payment info should be handled entirely by the payment terminal and provider and everest should only receive
        // anonymized/randomized tokens for authentication (AuthTokenProvider).
        constraints.add(ifStatusThenStatus(List.of("PaymentInformation"), List.of("Anonymized")));

        // Requirement #39:
        // Everest needs to make sure that data that has been transferred to cloud systems like payment providers is not written
        // into logs stored on the charger.
        constraints.add(neverFlowsToComponent(List.of("Token"), List.of("Logs")));
        constraints.add(neverFlowsToComponent(List.of("PaymentInformation"), List.of("Logs")));

        // Requirement #41:
        // No unauthorized persons shall be able to access more than the user GUI of a charging station"
        constraints.add(onlyActorWithStatusHasAccessToLocation(List.of("Authorized"), List.of("ChargingStation")));

        // Requirement #46:
        // The system (the whole charger) should not hold PII data unencrypted at rest. eg. mac addresses in log files (but a
        // mac address as PII is debatable).
        constraints.add(withStatusWithoutStatusNeverFlowsToComponent(List.of("Sensitive"), List.of("Encrypted"), List.of("Logs")));

        // Requirement #60:
        // No identifying information is available to unauthorized persons.
        constraints.add(onlyActorWithStatusHasAccessToDataWithStatusWithoutStatus(List.of("Authorized"), List.of("Sensitive"), List.of("Encrypted")));

        // Requirement #62:
        // Only a support engineer shall be able to change the EVerest config files via a Software (SW) update.
        constraints.add(onlyActorHasAccessToStatus(List.of("Engineer"), List.of("Firmware")));

        // Requirement #63:
        // No unauthorized persons shall be able to access more than the user GUI of a charging station.
        constraints.add(onlyActorWithStatusHasAccessToLocation(List.of("Authorized"), List.of("ChargingStation")));

        // Requirement #76:
        // EVerest should support secure storage of private keys and certificates used for TLS to backend and EV via TPM in its
        // common component libevse-security.
        constraints.add(withStatusWithoutStatusNeverFlowsTo(List.of("Certificate"), List.of("Encrypted"), List.of("TPM")));
        constraints.add(neverFlowsTo(List.of("Certificate"), List.of("LocalStorage", "External")));
        constraints.add(neverFlowsToComponent(List.of("Certificate"), List.of("Logs")));

        // Requirement #80:
        // EVerest should store privacy sensitive information encrypted, if stored at all. Privacy sensitive information
        // includes customer and contact identification means including ID tags, EV MAC addresses, and bank information.
        constraints.add(withStatusWithoutStatusNeverFlowsToComponent(List.of("Sensitive"), List.of("Encrypted"), List.of("Logs")));
        constraints.add(withStatusWithoutStatusNeverFlowsTo(List.of("Sensitive"), List.of("Encrypted"), List.of("LocalStorage", "TPM")));

        // Requirement #81:
        // EVerest shall not sore any privacy sensitive information in a log file. Privacy sensitive information includes
        // customer and contact identification means including ID tags, EV MAC addresses, and bank information."
        constraints.add(withStatusWithoutStatusNeverFlowsToComponent(List.of("Sensitive"), List.of("Encrypted"), List.of("Logs")));

        // Requirement #87:
        // Engineers that locally need access to a charger shall use a secure authentication.
        constraints.add(onlyActorWithStatusHasAccessToLocation(List.of("Authorized"), List.of("ChargingStation")));

        // Requirement #88:
        // Remote access to the charger, if at all, shall only be possible by a user that is authenticated securely.
        constraints.add(onlyActorWithStatusHasAccessToLocation(List.of("Authorized"), List.of("ChargingStation")));

        // Requirement #92:
        // The system component shall not expose the charger's firmware to external access.
        constraints.add(onlyFlowsTo(List.of("Firmware"), List.of("ChargingStation")));

        return constraints;
    }

    @Override
    public List<ExpectedViolation> getExpectedViolations() {
        return List.of(
                new ExpectedViolation(1, PCMIdentifier.of("_zTBm4DOgEe-thK3JRY5OhA"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("reservation",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Encrypted"),
                                        new ExpectedCharacteristic("Status", "Sensitive")))),
                new ExpectedViolation(1, PCMIdentifier.of("_hM4rcDOhEe-thK3JRY5OhA"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("reservation",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Encrypted"),
                                        new ExpectedCharacteristic("Status", "Sensitive")))),
                new ExpectedViolation(1, PCMIdentifier.of("_fZbbMEODEe-0EeXsSysZ9A", true),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("reservation", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"),
                                new ExpectedCharacteristic("Status", "Encrypted"), new ExpectedCharacteristic("Status", "Sensitive")),
                                "reservation_result",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Encrypted"),
                                        new ExpectedCharacteristic("Status", "Sensitive"), new ExpectedCharacteristic("Status", "Anonymized")))),
                new ExpectedViolation(1, PCMIdentifier.of("_JyylAigbEe-0jJudbTQUyQ"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("reservation_id", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(1, PCMIdentifier.of("_JyzMECgbEe-0jJudbTQUyQ"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("reservation_id", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(1, PCMIdentifier.of("_fZbbMEODEe-0EeXsSysZ9A", false),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("reservation_id", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")), "reservation",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Encrypted"),
                                        new ExpectedCharacteristic("Status", "Sensitive")),
                                "reservation_result",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Encrypted"),
                                        new ExpectedCharacteristic("Status", "Sensitive"), new ExpectedCharacteristic("Status", "Anonymized")))),
                new ExpectedViolation(1, PCMIdentifier.of("_DFF9EFUBEe-wM6yAw99YUA"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("reservation_id", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")), "reservation",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Encrypted"),
                                        new ExpectedCharacteristic("Status", "Sensitive")),
                                "reservation_result",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Encrypted"),
                                        new ExpectedCharacteristic("Status", "Sensitive"), new ExpectedCharacteristic("Status", "Anonymized")))),
                new ExpectedViolation(1, PCMIdentifier.of("_zTBm4TOgEe-thK3JRY5OhA"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("RETURN",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Encrypted"),
                                        new ExpectedCharacteristic("Status", "Sensitive"), new ExpectedCharacteristic("Status", "Anonymized")),
                                "reservation_id", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")), "reservation",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Encrypted"),
                                        new ExpectedCharacteristic("Status", "Sensitive")),
                                "reservation_result",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Encrypted"),
                                        new ExpectedCharacteristic("Status", "Sensitive"), new ExpectedCharacteristic("Status", "Anonymized")))),
                new ExpectedViolation(2, PCMIdentifier.of("_xHsrAE0LEe-j2Yo8i_9OIA"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("msg", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(2, PCMIdentifier.of("_0oJ6AE0LEe-j2Yo8i_9OIA", true),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("msg", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(2, PCMIdentifier.of("_Nl18oDxjEe-LH4VHtqs82Q"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("request", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(2, PCMIdentifier.of("_xkBJQDxlEe-LH4VHtqs82Q", true),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("request", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(2, PCMIdentifier.of("_xkBJQDxlEe-LH4VHtqs82Q", false),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("request",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Sensitive")))),
                new ExpectedViolation(2, PCMIdentifier.of("_xHsrAU0LEe-j2Yo8i_9OIA"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("request", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")), "msg",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(2, PCMIdentifier.of("_AsLbMDxmEe-LH4VHtqs82Q"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("request",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Sensitive")),
                                "firmwareFile", List.of(new ExpectedCharacteristic("Status", "Firmware")))),
                new ExpectedViolation(2, PCMIdentifier.of("_Nl18oTxjEe-LH4VHtqs82Q"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("request",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Sensitive")),
                                "firmwareFile", List.of(new ExpectedCharacteristic("Status", "Firmware")), "RETURN",
                                List.of(new ExpectedCharacteristic("Status", "Firmware")))),
                new ExpectedViolation(2, PCMIdentifier.of("_0oJ6AE0LEe-j2Yo8i_9OIA", false),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("msg", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")), "request",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")), "RETURN",
                                List.of(new ExpectedCharacteristic("Status", "Firmware")))),
                new ExpectedViolation(3, PCMIdentifier.of("_17NEkDxoEe-LH4VHtqs82Q"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("resetType", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(3, PCMIdentifier.of("_17NEkTxoEe-LH4VHtqs82Q"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("resetType", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(5, PCMIdentifier.of("_YLIkwEpjEe-QePQiuhxJfA"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation"),
                                new ExpectedCharacteristic("Component", "PN532TokenProvider")),
                        Map.of("token",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Token"),
                                        new ExpectedCharacteristic("Status", "Sensitive")))),
                new ExpectedViolation(5, PCMIdentifier.of("_06BDsEpmEe-QePQiuhxJfA", true),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation"),
                                new ExpectedCharacteristic("Component", "PN532TokenProvider")),
                        Map.of("token",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Token"),
                                        new ExpectedCharacteristic("Status", "Sensitive")))),
                new ExpectedViolation(5, PCMIdentifier.of("_WYPuAEpnEe-QePQiuhxJfA"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("providedToken",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Token"),
                                        new ExpectedCharacteristic("Status", "Sensitive")))),
                new ExpectedViolation(5, PCMIdentifier.of("_YM6JkEpnEe-QePQiuhxJfA", true),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("providedToken",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Token"),
                                        new ExpectedCharacteristic("Status", "Sensitive")))),
                new ExpectedViolation(5, PCMIdentifier.of("_K2QRMH8PEe-Ips9OktXuUA"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("provided_token",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Token"),
                                        new ExpectedCharacteristic("Status", "Sensitive")))),
                new ExpectedViolation(5, PCMIdentifier.of("_K2QRMX8PEe-Ips9OktXuUA"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("provided_token",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Token"),
                                        new ExpectedCharacteristic("Status", "Sensitive")))),
                new ExpectedViolation(5, PCMIdentifier.of("_YM6JkEpnEe-QePQiuhxJfA", false),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")), Map.of(
                                "provided_token", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"),
                                        new ExpectedCharacteristic("Status", "Token"), new ExpectedCharacteristic("Status", "Sensitive")),
                                "providedToken",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Token"),
                                        new ExpectedCharacteristic("Status", "Sensitive")))),
                new ExpectedViolation(5, PCMIdentifier.of("_oCeSgVY4Ee-lU5UkonmXoQ"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation"), new ExpectedCharacteristic("Component", "Logs")),
                        Map.of("logmessage",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Token"),
                                        new ExpectedCharacteristic("Status", "Sensitive")))),
                new ExpectedViolation(5, PCMIdentifier.of("_oCeSglY4Ee-lU5UkonmXoQ"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation"), new ExpectedCharacteristic("Component", "Logs")),
                        Map.of("logmessage",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Token"),
                                        new ExpectedCharacteristic("Status", "Sensitive")))),
                new ExpectedViolation(5, PCMIdentifier.of("_06BDsEpmEe-QePQiuhxJfA", false),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation"),
                                new ExpectedCharacteristic("Component", "PN532TokenProvider")),
                        Map.of("providedToken",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Token"),
                                        new ExpectedCharacteristic("Status", "Sensitive")),
                                "token",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Token"),
                                        new ExpectedCharacteristic("Status", "Sensitive")))),
                new ExpectedViolation(5, PCMIdentifier.of("_Z3mS0HBQEe-sCL8KfTMM7A", true),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation"),
                                new ExpectedCharacteristic("Component", "PN532TokenProvider")),
                        Map.of("providedToken",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Token"),
                                        new ExpectedCharacteristic("Status", "Sensitive")),
                                "token",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Token"),
                                        new ExpectedCharacteristic("Status", "Sensitive")))),
                new ExpectedViolation(5, PCMIdentifier.of("_oCeSgVY4Ee-lU5UkonmXoQ"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation"), new ExpectedCharacteristic("Component", "Logs")),
                        Map.of("logmessage",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Token"),
                                        new ExpectedCharacteristic("Status", "Sensitive")))),
                new ExpectedViolation(5, PCMIdentifier.of("_oCeSglY4Ee-lU5UkonmXoQ"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation"), new ExpectedCharacteristic("Component", "Logs")),
                        Map.of("logmessage",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Token"),
                                        new ExpectedCharacteristic("Status", "Sensitive")))),
                new ExpectedViolation(5, PCMIdentifier.of("_Z3mS0HBQEe-sCL8KfTMM7A", false),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation"),
                                new ExpectedCharacteristic("Component", "PN532TokenProvider")),
                        Map.of("logmessage",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Token"),
                                        new ExpectedCharacteristic("Status", "Sensitive")),
                                "providedToken",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Token"),
                                        new ExpectedCharacteristic("Status", "Sensitive")),
                                "token",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Token"),
                                        new ExpectedCharacteristic("Status", "Sensitive")))),
                new ExpectedViolation(5, PCMIdentifier.of("_YLIkwUpjEe-QePQiuhxJfA"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation"),
                                new ExpectedCharacteristic("Component", "PN532TokenProvider")),
                        Map.of("logmessage",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Token"),
                                        new ExpectedCharacteristic("Status", "Sensitive")),
                                "providedToken",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Token"),
                                        new ExpectedCharacteristic("Status", "Sensitive")),
                                "token",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Token"),
                                        new ExpectedCharacteristic("Status", "Sensitive")))),
                new ExpectedViolation(5, PCMIdentifier.of("_oCeSgVY4Ee-lU5UkonmXoQ"),
                        List.of(new ExpectedCharacteristic("Component", "Logs"), new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("logmessage",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Sensitive"),
                                        new ExpectedCharacteristic("Status", "Token")))),
                new ExpectedViolation(5, PCMIdentifier.of("_oCeSglY4Ee-lU5UkonmXoQ"),
                        List.of(new ExpectedCharacteristic("Component", "Logs"), new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("logmessage",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Sensitive"),
                                        new ExpectedCharacteristic("Status", "Token")))),
                new ExpectedViolation(6, PCMIdentifier.of("_DL6XoigbEe-0jJudbTQUyQ"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("connector_id", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(6, PCMIdentifier.of("_DL6-sCgbEe-0jJudbTQUyQ"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("connector_id", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(6, PCMIdentifier.of("_nDVhIT7IEe-HHLw-F3Rezw"),
                        List.of(new ExpectedCharacteristic("Component", "API"), new ExpectedCharacteristic("Location", "ChargingStation"),
                                new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("data", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(6, PCMIdentifier.of("_9qmVwD90Ee-T3I6VeO-56g", true),
                        List.of(new ExpectedCharacteristic("Component", "API"), new ExpectedCharacteristic("Location", "ChargingStation"),
                                new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("data", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(6, PCMIdentifier.of("_9qmVwD90Ee-T3I6VeO-56g", false),
                        List.of(new ExpectedCharacteristic("Component", "API"), new ExpectedCharacteristic("Location", "ChargingStation"),
                                new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("connector_id", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")), "data",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(6, PCMIdentifier.of("_nDVhIj7IEe-HHLw-F3Rezw"),
                        List.of(new ExpectedCharacteristic("Component", "API"), new ExpectedCharacteristic("Location", "ChargingStation"),
                                new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("connector_id", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")), "data",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(7, PCMIdentifier.of("_BlU0USgbEe-0jJudbTQUyQ"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("connector_id", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(7, PCMIdentifier.of("_BlU0UCgbEe-0jJudbTQUyQ"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("connector_id", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(7, PCMIdentifier.of("_nDcO0T7IEe-HHLw-F3Rezw"),
                        List.of(new ExpectedCharacteristic("Component", "API"), new ExpectedCharacteristic("Location", "ChargingStation"),
                                new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("data", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(7, PCMIdentifier.of("_E1-XUD91Ee-T3I6VeO-56g", true),
                        List.of(new ExpectedCharacteristic("Component", "API"), new ExpectedCharacteristic("Location", "ChargingStation"),
                                new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("data", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(7, PCMIdentifier.of("_E1-XUD91Ee-T3I6VeO-56g", false),
                        List.of(new ExpectedCharacteristic("Component", "API"), new ExpectedCharacteristic("Location", "ChargingStation"),
                                new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("connector_id", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")), "data",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(7, PCMIdentifier.of("_nDcO0j7IEe-HHLw-F3Rezw"),
                        List.of(new ExpectedCharacteristic("Component", "API"), new ExpectedCharacteristic("Location", "ChargingStation"),
                                new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("data", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")), "connector_id",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(10, PCMIdentifier.of("_L3MFsCgbEe-0jJudbTQUyQ"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("value",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"),
                                        new ExpectedCharacteristic("Status", "Anonymized")))),
                new ExpectedViolation(10, PCMIdentifier.of("_13ijACjUEe-vi6u5C9ZFsw"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("value",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"),
                                        new ExpectedCharacteristic("Status", "Anonymized")))),
                new ExpectedViolation(10, PCMIdentifier.of("_L3MFsSgbEe-0jJudbTQUyQ"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("local_energy_limits",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Anonymized")),
                                "value",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"),
                                        new ExpectedCharacteristic("Status", "Anonymized")))),
                new ExpectedViolation(10, PCMIdentifier.of("_nChBwT7IEe-HHLw-F3Rezw"),
                        List.of(new ExpectedCharacteristic("Component", "API"), new ExpectedCharacteristic("Location", "ChargingStation"),
                                new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("data", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(10, PCMIdentifier.of("_z8B0ID9zEe-T3I6VeO-56g", true),
                        List.of(new ExpectedCharacteristic("Component", "API"), new ExpectedCharacteristic("Location", "ChargingStation"),
                                new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("data", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(10, PCMIdentifier.of("_z8B0ID9zEe-T3I6VeO-56g", false),
                        List.of(new ExpectedCharacteristic("Component", "API"), new ExpectedCharacteristic("Location", "ChargingStation"),
                                new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("data", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")), "value",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"),
                                        new ExpectedCharacteristic("Status", "Anonymized")))),
                new ExpectedViolation(10, PCMIdentifier.of("_nChBwj7IEe-HHLw-F3Rezw"),
                        List.of(new ExpectedCharacteristic("Component", "API"), new ExpectedCharacteristic("Location", "ChargingStation"),
                                new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("data", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")), "value",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"),
                                        new ExpectedCharacteristic("Status", "Anonymized")))),
                new ExpectedViolation(11, PCMIdentifier.of("_L3MFsCgbEe-0jJudbTQUyQ"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("value",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"),
                                        new ExpectedCharacteristic("Status", "Anonymized")))),
                new ExpectedViolation(11, PCMIdentifier.of("_13ijACjUEe-vi6u5C9ZFsw"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("value",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"),
                                        new ExpectedCharacteristic("Status", "Anonymized")))),
                new ExpectedViolation(11, PCMIdentifier.of("_L3MFsSgbEe-0jJudbTQUyQ"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("local_energy_limits",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Anonymized")),
                                "value",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"),
                                        new ExpectedCharacteristic("Status", "Anonymized")))),
                new ExpectedViolation(11, PCMIdentifier.of("_nCvrQT7IEe-HHLw-F3Rezw"),
                        List.of(new ExpectedCharacteristic("Component", "API"), new ExpectedCharacteristic("Location", "ChargingStation"),
                                new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("data", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(11, PCMIdentifier.of("_bv2HgD90Ee-T3I6VeO-56g", true),
                        List.of(new ExpectedCharacteristic("Component", "API"), new ExpectedCharacteristic("Location", "ChargingStation"),
                                new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("data", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(11, PCMIdentifier.of("_bv2HgD90Ee-T3I6VeO-56g", false),
                        List.of(new ExpectedCharacteristic("Component", "API"), new ExpectedCharacteristic("Location", "ChargingStation"),
                                new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("data", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")), "value",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"),
                                        new ExpectedCharacteristic("Status", "Anonymized")))),
                new ExpectedViolation(11, PCMIdentifier.of("_nCvrQj7IEe-HHLw-F3Rezw"),
                        List.of(new ExpectedCharacteristic("Component", "API"), new ExpectedCharacteristic("Location", "ChargingStation"),
                                new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("data", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")), "value",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"),
                                        new ExpectedCharacteristic("Status", "Anonymized")))),
                new ExpectedViolation(12, PCMIdentifier.of("_jwf4oSNDEe-9BLE8eIVxZg"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("connector_id", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(12, PCMIdentifier.of("_QrtjoSNEEe-9BLE8eIVxZg"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("connector_id", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(12, PCMIdentifier.of("_nDOzcT7IEe-HHLw-F3Rezw"),
                        List.of(new ExpectedCharacteristic("Component", "API"), new ExpectedCharacteristic("Location", "ChargingStation"),
                                new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("data", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(12, PCMIdentifier.of("_vVDzkD90Ee-T3I6VeO-56g", true),
                        List.of(new ExpectedCharacteristic("Component", "API"), new ExpectedCharacteristic("Location", "ChargingStation"),
                                new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("data", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(12, PCMIdentifier.of("_vVDzkD90Ee-T3I6VeO-56g", false),
                        List.of(new ExpectedCharacteristic("Component", "API"), new ExpectedCharacteristic("Location", "ChargingStation"),
                                new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("data", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")), "connector_id",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(12, PCMIdentifier.of("_nDOzcj7IEe-HHLw-F3Rezw"),
                        List.of(new ExpectedCharacteristic("Component", "API"), new ExpectedCharacteristic("Location", "ChargingStation"),
                                new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("data", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")), "connector_id",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(13, PCMIdentifier.of("_ddU6gEWqEe-vaMxYiHuBmw"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("reservation_id",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Sensitive"),
                                        new ExpectedCharacteristic("Status", "Anonymized")))),
                new ExpectedViolation(13, PCMIdentifier.of("_mf_04EWqEe-vaMxYiHuBmw", true),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("reservation_id",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Sensitive"),
                                        new ExpectedCharacteristic("Status", "Anonymized")))),
                new ExpectedViolation(13, PCMIdentifier.of("_s31PoDOhEe-thK3JRY5OhA"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("reservation_id",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Sensitive"),
                                        new ExpectedCharacteristic("Status", "Anonymized")))),
                new ExpectedViolation(13, PCMIdentifier.of("_2SADIEWsEe-vaMxYiHuBmw"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("reservation_id",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Sensitive"),
                                        new ExpectedCharacteristic("Status", "Anonymized")))),
                new ExpectedViolation(13, PCMIdentifier.of("_Byo0wEWtEe-vaMxYiHuBmw", true),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("reservation_id",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Sensitive"),
                                        new ExpectedCharacteristic("Status", "Anonymized")))),
                new ExpectedViolation(13, PCMIdentifier.of("_Byo0wEWtEe-vaMxYiHuBmw", false),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("reservation_id",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Sensitive"),
                                        new ExpectedCharacteristic("Status", "Anonymized")))),
                new ExpectedViolation(13, PCMIdentifier.of("_LUBEYEWtEe-vaMxYiHuBmw"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("reservation_id",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized"), new ExpectedCharacteristic("Status", "Sensitive"),
                                        new ExpectedCharacteristic("Status", "Anonymized")))),
                new ExpectedViolation(14, PCMIdentifier.of("_zRGngE0LEe-j2Yo8i_9OIA"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("msg", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(14, PCMIdentifier.of("_AILVsE0OEe-j2Yo8i_9OIA", true),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("msg", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(14, PCMIdentifier.of("_92x60E0NEe-j2Yo8i_9OIA"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("request", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(14, PCMIdentifier.of("_Uj7oME0OEe-j2Yo8i_9OIA", true),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("request", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(14, PCMIdentifier.of("_Uj7oME0OEe-j2Yo8i_9OIA", false),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("request", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(14, PCMIdentifier.of("_zRGngU0LEe-j2Yo8i_9OIA"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("msg", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")), "request",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
                new ExpectedViolation(14, PCMIdentifier.of("_XQriUU0OEe-j2Yo8i_9OIA"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("request", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")), "firmwareFile",
                                List.of(new ExpectedCharacteristic("Status", "Signed"), new ExpectedCharacteristic("Status", "Firmware")))),
                new ExpectedViolation(14, PCMIdentifier.of("_92x60U0NEe-j2Yo8i_9OIA"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("request", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")), "firmwareFile",
                                List.of(new ExpectedCharacteristic("Status", "Signed"), new ExpectedCharacteristic("Status", "Firmware")), "RETURN",
                                List.of(new ExpectedCharacteristic("Status", "Signed"), new ExpectedCharacteristic("Status", "Firmware")))),
                new ExpectedViolation(14, PCMIdentifier.of("_AILVsE0OEe-j2Yo8i_9OIA", false),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("request", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")), "msg",
                                List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")), "RETURN",
                                List.of(new ExpectedCharacteristic("Status", "Signed"), new ExpectedCharacteristic("Status", "Firmware"))))

        );
    }

    @Override
    public String toString() {
        return this.getModelName();
    }
}

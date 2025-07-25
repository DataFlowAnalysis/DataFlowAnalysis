# 📊 Diagram: Corona Warn App (CWA)

## 🔗 Link to Original Paper/Article
[View Full Main Source](<https://github.com/corona-warn-app/cwa-documentation/blob/main/solution_architecture.md>)

## 📝 Description
This diagram illustrates the complex data flow of the Corona-Warn-App (CWA). The architecture is divided into three main components:

1. **Corona-Warn-App**: Runs on the user's mobile phone. This component is responsible for scanning nearby users' keys via *Bluetooth Low Energy (BLE)*, interacting with the *Exposure Notification Framework*, and communicating with the *Data Donation Server*.

2. **Verification Server**: Verifies test results received from the *Test Result Server*.

3. **Corona-Warn-App Server**: Receives diagnosis keys from the CWA and analytics data from the *Data Donation Server*. It stores this data and, via the *Content Delivery Network (CDN)*, makes it available to other users, allowing them to determine whether they have been in contact with someone who tested positive.

## ⚠️ Violated Constraints (If Any)
Although no violations were found in the original CWA architecture, we have slightly modified the diagram to produce two alternate versions in which violations are introduced:

- The first diagram, **RPIViolation**, illustrates a breach in which **RPIs (Rolling Proximity Identifiers)** flow into the CWA Server. This is a clear violation, as RPIs could potentially be linked back to individuals, undermining the anonymity and privacy principles of the original CWA.

  1. `data Identifiers.RPI neverFlows vertex Server.CWAppServer`

- The second diagram, **PersonalDataViolation**, shows a case where **personal data** from the user's mobile device flows into the CWA (via the Verification Server). This is another violation of the strict privacy guarantees provided by the real CWA.

  1. `data Identifiers.PersonalData neverFlows vertex Server.CWApp,Server.VerificationServer,Server.TestResultServer,Server.DDServer,Server.CWAppServer`









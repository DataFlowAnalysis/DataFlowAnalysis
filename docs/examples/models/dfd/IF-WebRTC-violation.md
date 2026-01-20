# 📊 Model: (InformationFlow-WebRTC-violation)

::: tip Available Online
This model is available to view using the online editor!
<VPButton text="Open In Online Editor" href="https://editor.dataflowanalysis.org/?file=https://raw.githubusercontent.com/DataFlowAnalysis/DataFlowAnalysis/refs/heads/main/bundles/org.dataflowanalysis.examplemodels/scenarios/dfd/IF-WebRTC-violation/WebRTC-violation.json"></VPButton>
::: 

## 🔗 Link to Original Paper/Article
[View Source](https://ieeexplore.ieee.org/document/8703905)
[Open Example Model in Example Models Bundle](https://github.com/DataFlowAnalysis/DataFlowAnalysis/tree/main/bundles/org.dataflowanalysis.examplemodels/scenarios/dfd/IF-WebRTC-violation)

## 📝 Short Description
The case covers a simplified version of the WebRTC protocol.

## 🔤 Abbreviations
- `WebRTC`: Web Real-Time Communication
- `STUN`: Session Traversal Utilities for NAT
- `NAT`: Network Address Translator

## 📖 Extensive Description
__Alice__ and __Bob__ want to communicate. They exchange ports via STUN servers (__publish_port__ and __receive_port__) and exchange session data via a signaling server (__dispatch_initial_session_data__ and __dispatch_response_session_data__) to initiate a session. These servers are in the **Zone** `Attack`. After that, they can send and receive media by __create_media_package__ and __unpack_media_package__. The exchanged session data and media is encrypted. Encrypted data flows have a **Level** of `Low` but also a ContainedClassification which indicates the true Level of the unencrypted flow.

## 🏷️ Label Description
### 🗂️ Data Labels:
- **Level**: There are two types of levels: `High` and `Low`.
- **ContainedClassification**: This label marks the **Level** of an encrypted data flow. It can be `High` or `Low`.
### 🏷️ Node Labels:
- **Zone**: There are two zones in this model: `Attack` and `Trust`. A node always part of one of these zones.

## ⚠️ Constraints(if any)
### Safety
The fundamental requirement is that system parts or actors in the attack zone must not have access to data classified High:
- `Safety: data Level.High neverFlows vertex Zone.Attack`

## 🚨 Violations
The error introduced in the case is that the session data of Bob can be sent unencrypted to the signaling server. We name the violating flow *bob_session_data* to ease finding the flow in the flow stack. 


<script setup>
import { VPButton } from 'vitepress/theme'
</script>

# Model and DSL Examples
## DFD Example Models
| Name                                                                                                              | Model Type |
|-------------------------------------------------------------------------------------------------------------------| ---------- |
| [AppleWallet](/examples/models/dfd/apple-wallet)                                                                  | DFD        |
| [ABAC (Access Control)](/examples/models/dfd/AC-ABAC-violation)                                                   | DFD        |
| [Conference Management App](/examples/models/dfd/cma)                                                             | DFD        |
| [Component Testing](/examples/models/dfd/component-testing)                                                       | DFD        |
| [ContactSMS (Access Control)](/examples/models/dfd/AC-ContactSMS-violation)                                       | DFD        |
| [ContactSMS (Information Flow)](/examples/models/dfd/IF-ContactSMS-no-violation)                                     | DFD        |
| [Corona Warn App](/examples/models/dfd/cwa)                                                                       | DFD        |
| [DAC (Access Control)](/examples/models/dfd/AC-DAC-violation)                                                     | DFD        |
| [DistanceTracker (Access Control)](/examples/models/dfd/AC-DistanceTracker-violation)                             | DFD        |
| [DistanceTracker (Information Flow)](/examples/models/dfd/IF-DistanceTracker-violation)                           | DFD        |
| [DocProc](/examples/models/dfd/doc-proc)                                                                          | DFD        |
| [ECUUpdate](/examples/models/dfd/ecu-update)                                                                      | DFD        |
| [FriendMap (Information Flow)](/examples/models/dfd/IF-FriendMap-violation)                                       | DFD        |
| [Hippa](/examples/models/dfd/hippa)                                                                               | DFD        |
| [Hospital (Information Flow)](/examples/models/dfd/IF-Hospital-violation)                                         | DFD        |
| [JPMail (Information Flow)](/examples/models/dfd/IF-JPMail-violation)                                             | DFD        |
| [Kulturpass](/examples/models/dfd/kulturpass)                                                                     | DFD        |
| [MilitaryAircraftController (Access Control)](/examples/models/dfd/AC-MAC-violation)                              | DFD        |
| [MiniTwit](/examples/models/dfd/mini-twit)                                                                        | DFD        |
| [PatientMonitoringSystem-Monitoring](/examples/models/dfd/patient-monitoring-system-monitoring)                   | DFD        |
| [PatientMonitoringSystem-Overview](/examples/models/dfd/patient-monitoring-system-overview)                       | DFD        |
| [PrivateTaxi (Information Flow)](/examples/models/dfd/IF-PrivateTaxi-violation)                                | DFD        |
| [SmartSpeakerSystem-Storage](/examples/models/dfd/smart-speaker-storage-violation)                                | DFD        |
| [TravelPlanner (Access Control)](/examples/models/dfd/AC-TravelPlanner-violation)                                 | DFD        |
| [TravelPlanner (Information Flow)](/examples/models/dfd/IF-TravelPlanner-violation)                               | DFD        |
| [VWCariad](/examples/models/dfd/vw-cariad)                                                                        | DFD        |
| [WebRTC (Information Flow)](/examples/models/dfd/IF-WebRTC-violation)                                             | DFD        |
| [BranchingOnlineShop](/examples/models/pcm/branching-online-shop)                                                 | PCM        |
| [CoCar](/examples/models/pcm/co-car)                                                                              | PCM        |
| [Corona Warn App](/examples/models/pcm/cwa)                                                                       | PCM        |
| [EVerest](/examples/models/pcm/everest)                                                                           | PCM        |
| [InternationalOnlineShop](/examples/models/pcm/international-online-shop)                                         | PCM        |
| [MaaS](/examples/models/pcm/maas)                                                                                 | PCM        |
| [TravelPlanner](/examples/models/pcm/travel-planner)                                                              | PCM        |



## DSL Examples

### Modelling simple flow rules

Modelling a constraint matching flows from an originating node that has data properties A to a vertex with vertex properties B:

```
data A neverFlows vertex B
```



::: tip Examples 

Sensitive Data never flows to a server outside of the EU:

```
data Type.Sensitive neverFlows vertex Location.nonEU
```



Internal Data never flows to the user:

```
data Type.Internal neverFlows vertex Role.User
```

:::



### Modelling Access Control 

Modelling a constraint matching access control rules for RequiredRoles and AssignedRoles:

```
data AssignedRoles.$Assigned 
neverFlows 
vertex RequiredRoles.$Required
where
present $Assigned
present $Required
empty intersection($Assigned,$Required)

```




# 📊 Diagram: MiniTwit Case-Study

::: tip Available Online
This model is available to view using the online editor!
<VPButton text="Open In Online Editor" href="https://editor.dataflowanalysis.org/?file=https://raw.githubusercontent.com/DataFlowAnalysis/DataFlowAnalysis/refs/heads/main/bundles/org.dataflowanalysis.examplemodels/scenarios/dfd/MiniTwitCaseStudy/MiniTwitCaseStudy.json"></VPButton>
::: 

## 🔗 Link to Original Paper/Article
[View Full Main Source](<https://www.research-collection.ethz.ch/handle/20.500.11850/641986>)


## 📝 Short Description
This diagram illustrates the data flow between the components/functionalities of the MiniTwit application from the study. The diagram is based on the code from the study, in which the nodes show different functionalities and labels show decorators that the functions had. This application behaves like a much simpler version of Twitter with the diagram showing the main functionalities, such as un/following users, diplaying the public/private timeline or adding a message. The diagram also shows the added components for privacy policies and consents implemented in the app, which was one of the main focus of the paper.

## 🔤 Abbreviations
- Ad/s: Advertisement

## 📖 Extensive Description (if possible)
The left side of the diagram shows the user entering its _credentials_ (_email_, _username_, and _password_) into the __Flask App__. From here on there are 2 possible flows of data, which depend on the user being already registered or not. In both flows the _credentials_ flow to the __Login Flask__ or __Register Flask__ nodes. Afterwards in the login case the _credentials_ flow to the __Check Credentials__ node where they are checked and consequentially to the __SQLite DB__ node. In the case of the registration flow the _credentials_ flow to the __After Register Hook__ and finally to the __SQLite DB__. From the __SQLite DB__ lots of different data flow to the __Current User__ node which then distributes the data to the nodes which contain the individual functionalities of the application. Starting from the top there is a flow of the _feed_messages_ data to the __Display Public Timeline__ node, which in turn generated the _ads_ data flow to the __Generate Ad General__ node. Directy beneath there are the __Unfollow User__ and __Follow User__ nodes which send the respective *un/follow_data* to the __Save To DB__ node so that this new information is saved. Another flow of data from the __Current User__ node is with the _purpose_, _user_data_ and _user_consents_ data flows which serve as input for the __Add Consent__ node and after the new consents have being added they flow to the __Save To DB__ node again to be stored. In a similar manner there is a flow of data of *user_consents* from the __Current User__ to the __Remove Consents__ node which then outputs the *actualized_consents* and flows again into the __Save To DB__ function. Right beneath that there is the __Add Message__ node which send the message data to the __Save To DB__ node. Furthermore there is the flow of the user_consents data again to the __Policy__ node, in which a user can view its consent policies, which then redirects this data to the __Privacy Template__ node where the consents are shown. Lastly there is the information flow from __Current User__ with the *follow_data*, *user_profile* data (from another user) and *users_messages* to the __Display Single User Timeline__ node, which redirects the information to the node __User Timeline Template__. 

## 🏷️ Label description

- ### 🗂️ Data Labels:
    - ### UserData:
        - __FeedMessages__: Messages that appear in the feed of a user
        - __FollowData__: Data of followed users
        - __UserMessages__: Messages of the given user
        - __UnfollowData__: Data containing the user that has been unfollowed
        - __UserProfile__: Another users profile data
        - __Message__: Messages data of a given user
        - __UserData__: Data of the user
        - __Purpose__: Action that the user wants to perform (and needs consent for)
        - __UserConsents__: List of consented actions/purposes of the user
        - __NewConsents__: List of updated actions/purposes of the user after adding a new consent
        - __Ads__: Advertisements of the application for a user
    
    - ### Credentials:
        - __Email__: Email of the user
        - __Password__: Password of the user
        - __Username__: User name of the user
        
- ### 🏷️ Node Labels:
    - ### Decorator:
        - __Secure__: Decorator that defines that given node (or function in the code) as secure
        - __UserRegistered__: Decorator that defines that for the given node (or function in the code) the user has to be registered
        - __LoginRequired__: Decorator that defines that for the given node (or function in the code) the user has to be logged in
    - ### ConsentedPurposes
        - __DisplayPublicTimeline__: The purpose of displaying the public timeline (feed) to the user is consented
        - __GenerateRelevantMarketingEntities__: The purpose of generating individual advertisements to the user is consented
        - __DisplayRelevantPosts__: The purpose of displaying the content from the followed user is consented

## ⚠️ Constraints
- Sensitive data from the user such as UnfollowData, FollowData, Message and NewConsents can not flow to an insecure node:

    1. __secure__: `data UserData.UnfollowData,UserData.FollowData,UserData.Message,UserData.NewConsents neverFlows vertex !Decorator.Secure`

- User messages data neverflows to a node which does not have the purpose/settings of DisplayRelevantPosts allowed:

    1. __purpose_display_sing_timeline__: `data UserData.UserMessages neverFlows vertex !ConsentedPurposes.DisplayRelevantPosts`

- Advertisements data neverflows to a node which does not have the purpose/setting of GenerateRelevantMaketingEntities allowed:

    2. __purpose_display_sing_timeline__: `data UserData.Ads neverFlows vertex !ConsentedPurposes.GenerateRelevantMarketingEntities`

- Feed Messages data from the user neverflow to a node which does not have the purpose/action of DisplayPublicTimeline allowed:

    3. __purpose_display_sing_timeline__: `data UserData.FeedMessages neverFlows vertex !ConsentedPurposes.DisplayPublicTimeline`

## 🚨 Violations
- __None__



<script setup>
import { VPButton } from 'vitepress/theme'
</script>



## 📝 Description
This diagram illustrates the data flow between the components/functionalities of the CMA application from the study. This application contains functionalities such as submitting manuscripts, assigning reviewers to the manuscripts (so that they become accepted papers), recommending papers, searching for papers, etc. The diagram also shows the added components for privacy policies and consents implemented in the app.

::: tip Available Online
This model is available to view using the online editor!
<VPButton text="Open In Online Editor" href="https://editor.dataflowanalysis.org/?file=https://raw.githubusercontent.com/DataFlowAnalysis/DataFlowAnalysis/refs/heads/main/bundles/org.dataflowanalysis.examplemodels/scenarios/dfd/CMACaseStudy/CMACaseStudy.json"></VPButton>
::: 

# 📊 Diagram: Conference Management App (CMA) Case-Study

## 🔗 Link to Original Paper/Article
[View Full Main Source](<https://www.research-collection.ethz.ch/handle/20.500.11850/641986>)

[Open Example Model in Example Models Bundle](https://github.com/DataFlowAnalysis/DataFlowAnalysis/tree/main/bundles/org.dataflowanalysis.examplemodels/scenarios/dfd/CMACaseStudy)


## 📝 Short Description
This diagram illustrates the data flow between the components/functionalities of the CMA application from the study. The diagram is based on the code from the study, in which the nodes show different functionalities and labels show decorators that the functions had. This application contains functionalities such as submitting manuscripts, assigning reviewers to the manuscripts (so that they become accepted papers), recommending papers, searching for papers, etc. The diagram also shows the added components for privacy policies and consents implemented in the app, which was one of the main focus of the paper.

## 🔤 Abbreviations
- None

## 📖 Extensive Description (if possible)
The left side of the diagram shows the user entering its _credentials_ (_email_, _username_, and _password_) into the __Flask App__. From here on there are 2 possible flows of data, which depend on the user being already registered or not. In both flows the _credentials_ flow to the __Login Flask__ or __Register Flask__ nodes. Afterwards in the login case the _credentials_ flow to the __Check Credentials__ node where they are checked and consequentially to the __SQLite DB__ node. In the case of the registration flow the _credentials_ flow to the __After Register Hook__ and finally to the __SQLite DB__. From this node there are many data flows which are forwarded to the __Current User__ node, from where they will flow to their respective functionality nodes. Starting from the top there is the flow of *candidate_reviewers* data to the __Show Candidate Reviewers__ node which forwards it to the __Calculate Conflicts__ node. Directly beneath the *manuscripts* data is forwarded to the __Retreive Manuscripts__ node which then forwards it to the __Calculate Conflicts__ node, which makes sure that given the *manuscripts* and the *candidate_reviewers* there are no conflicts of interest (a reviewer reviews its own paper). From this node the list of *candidate_reviewers* flows to the __Assign Reviewer__ node, which does the reviewer assignment. Then the "ready to review" manuscript flows to the __Review Papers__ node. Once the manuscript has been reviewed it flows to the __Save To DB__ node where it is saved as a (not yet accepted) *paper*. Continuing with the flows from the __Current USer__ node, it sends the *user_consents* data to the __Policy__ node which forwards it to the __Privacy Template__ where the user its able to see its current consent settings. In the next flow *user_consents* flow to the __Remove Consents__ node, which then forwards the *actualize_consents* to the __Save To DB__ node. Accordingly the next one is again the forwarding of the *user_consents* to the __Add Consents__ node, which forwards the *new_consents* to the __Save To DB__ node. Next, on the lower side of the diagram from the __Current User__ node, there are flows from an (accepted) *paper* to the __Retrieve Recent Papers__ node and __View Paper__ node. The first one then forwards the *paper* to the __Calculate Recommendations__ node which then forwards it to the __Recommend Papers__ node and lastly to the __Recommend Papers Template__ where the user can recommend a certain paper. In the case of the __View Paper__ node it also receives a *paper_request* data from the __Search Papers__ node and forwards the *paper* to the __View Paper Template__ where the user can read the paper. The last flow from the __Current User__ node is regarding the *reviewed paper* data (manuscript which has been reviewed) which flows to the __Accept Paper__ node. Here the reviewed paper is accepted and flows (as *accepted_paper*) to the __Save To DB__ node. Lastly there is a last node called __Submit Manuscript__ which sends a *manuscript* to the __Save To DB__ node. 

## 🏷️ Label description
- Its important to notice that in the code, and thus in the diagram a manuscript is a paper which has not been reviewed. This manuscript, when reviewed, is an unaccepted paper which can then be accepted.
- ### 🗂️ Data Labels:
    - ### DataType:
        - __AcceptedPaper__: Paper which has been accepted
        - __ConsentSettings__: List of consented actions/purposes of the user
        - __ReviewedPaper__: Paper/Manuscript which has been reviewed
        - __CandidateReviewers__: List of possible Reviewers
        - __Manuscript__: Paper that has to be reviewed and then accepted
        - __NewConsents__: List of updated actions/purposes of the user after adding a new consent
        - __Purpose__: Purpose of the user (action he/she is going to make)
    
    - ### Credentials:
        - __Email__: Email of the user
        - __Password__: Password of the user
        - __Username__: User name of the user
        
    - ### RequestTypes:
        - __Consent__: Request to get the consent of the user to do a given action
        - __Paper__: Request to get a paper
        - __Recommend__: Request to recommend a paper
        - __Candidates__: Request to get the candidate reviewers
        - __Review__: Request to review a paper
        - __Accept__: Request to accept a paper

- ### 🏷️ Node Labels:
    - ### Decorator:
        - __Secure__: Decorator that defines that given node (or function in the code) as secure
        - __UserRegistered__: Decorator that defines that for the given node (or function in the code) the user has to be registered
        - __LoginRequired__: Decorator that defines that for the given node (or function in the code) the user has to be logged in
    
    - ### ConsentedPurposes
        - __PublishPaper__: Purpose of publishing a paper
        - __AssignReviewer__: Purpose of assigning a reviewer
        - __RecommendPapers__: Purpose of recommending papers
        - __ViewPaper__: Purpose of viewing a paper

    - ### ResearcherTypeAllowed:
        - __Normal__: Normal user is allowed to do this action (and thus all user types)
        - __Committee__: Committee member (or Chair member) is allowed, but not normal member
        - __Chair__: Only chair member is allowed


## ⚠️ Constraints
- Sensitive data from the user such as ConsentSettings (old or new), AcceptedPaper, ReviewedPapr, CandidateReviewers, Manuscripts, and Purpose can not flow to an insecure node:

    1. __secure__: `data DataType.AcceptedPaper,DataType.ConsentSettings,DataType.ReviewedPaper,DataType.CandidateReviewers,DataType.Manuscript,DataType.NewConsents,DataType.Purpose neverFlows vertex !Decorator.Secure`

- CandidateReviewers data can not flow to a node which does not have the consented purpose of AssignReviewer:

    2. __purposes__: `data DataType.CandidateReviewers neverFlows vertex !ConsentedPurposes.AssignReviewer`

- AcceptedPaper data neverflows to a vertes without the consented purpose of RecommendPapers or ViewPaper:

    3. __purpose_recommed_paper__: `data DataType.AcceptedPaper neverFlows vertex !ConsentedPurposes.RecommendPapers,ConsentedPurposes.ViewPaper`
    
## 🚨 Violations
- __None__








<script setup>
import { VPButton } from 'vitepress/theme'
</script>



# 📊 Diagram: Apple Wallet Case Study

## 🔗 Link to Original Paper/Article


## 📝 Short Description
These diagrams represent the main functionalities of the Apple Pay architecture, which are the enrollment process of a credit/debit card in Apple pay as conducted by a carholder and the other diagram illustrates the process of payment with the phone in a contactless device. The diagrams and explanations have been taken from the above source.

## 🔤 Abbreviations
- POS: Point Of Sale
- NFC: Near Field Communication
- OTP: One-Time Payment (token)
- PAN: Primary Account Number

## 📖 Extensive Description (if possible)
Although a more complete and thorough description can be found step-by-step in the source above, here are the explanations for the most important components of the diagram.

First there is the part regarding the enrollment process. First the __Cardholder__ and the __User Identification__ nodes for the authentication process. Then there is one of the main nodes of the diagram which is __Apple Wallet__. This node receives the *card_information* (such as name, cvv, expiration date, etc) and forwards it to the __Check_Card__ node which, in case the card information is correct, forwards it to the __Apple Server__. This node then does a process of signing the *terms_and_conditions* with the __Cardholder__. When accepted, the __Apple Server__ then sends the id of the terms and conditions in addition with the cards CVV to the __Link And Provision__ node, which forwards the *device_information* (e.g. device model, phone number, approx. location) to the __Issuer Bank__. When this process is finished the __Download Pass File__ node sends a *pass_file* (which represents the card in the app) to the __Apple Wallet__  node which then passes the card information to the __Apple Pay App__ to end the process.

For te part regarding the contactless payment, first the __Terminal App__ node sends the *payment_data* to the __NFC Reader App__ node which resides in POS. This node then forwards this to the __NFC Controller__ node which in turn forwards it to the __Apple Pay App__. Then this last node exchanges the *transaction_information* with the __Apple Wallet__ node in exchange for the *card_information*. Then after the previously mentioned authentication process between __Cardholder__ and __User Identification__ the __Apple Pay App__ sends the *otp* to the __NFC Controller__. This token is forwarded through a series of nodes (__NFC Reader App__, __Terminal App__ an __Payment Processor__) till it ends up at the __Token Service__. This node then determines the *pan* usign the *otp* and it sends it to the __Payment Processor__, which forwards it in addition with the transaction details and cvv of the card to the __Payment Network__. This node finally sends this information to the __Issuer Bank__ as an *authorization_request*. After the __Issuer Bank__ authorizes the payment it sends and *authorization_response* to the __Terminal App__ of the device of the __Cardholder__ thorugh a series of nodes (__Payment Network__, __Payment Processor__).
 
## 🏷️ Label description

- ### 🗂️ Data Labels:
    - ### CardholderStatus:
        - __Authenticated__: Cardholder has been correctly authenticated
        - __Authorized__: Cardholder has been correctly authorizes

    - ### CardInformation:
        - __Name__: Name of the cardholder
        - __Number__: Number of the card
        - __ExpirationDate__: Expiration date of the card
        - __CVV__: CVV code of the card

    - ### TermsConditions:
        - __Accepted__: Signed terms & conditions
        - __Normal__: Terms & conditions before being accepted
        - __Id__: Identifier of the terms & conditions

    - ### DeviceInfomation:
        - __Model__: Model of the cardholders phone
        - __PhoneNumber__: Phone number of the cardholder
        - __Location__: Coarse location of the device

    - ### PassFile:
        - __PassFile__: Pass file which represents the card in the Wallet App
    
    - ### PaymentData:
        - __Amount__: Amount of money payed in the transaction
        - __Token__: Payment token
        - __Location__: Location where the payment has taken place
        - __Others__: Other extra information from the payment
        - __Authorization__: Payment's authorization
    
    - ### Tokens:
        - __OTP__: One-Time Payment Token
        - __PAN__: Primary Account Number

    - ### RequestTypes:
        - __Authentication__: Request for an authentication
        - __Authorization__: Request for an authorization

- ### 🏷️ Node Labels:
    - ### Secure:
        - __Enclave__: This node belongs to a secure enclave
        - __Element__: This node is a secured element

    - ### Location:
        - __PointOfSale__: This node resides in the POS
        - __CardholderDevice__: This node resides in the Cardholders device
    
    - ### Module:
        - __TerminalApp__: This node is part of the Terminal App component
        - __AppleServer__: This node is part of the Apple Server component
        
## ⚠️ Constraints
- The Authentication and Authorization Request types should never flow to a node which is not in a secured enclave 

    1. __Auth_request__: `data RequestTypes.Authentication,RequestTypes.Authorization neverFlows vertex !Secure.Enclave`

## 🚨 Violations
- __None__











The KSO Template app was created to serve two main purposes:

1. As a sales demonstration app
2. To provide a fully functional steel thread application to start KSO projects from.

As a steel thread application it needs to provide:

- more functionality than a typical "hello world" application
- enough functionality that it serves as a useful onboarding tool for backend, gateway, and UI development
- enough coverage of our template design system to demonstrate that it is both comprehensive and instructive
- a demonstration that CI/CD pipelines for services, the gateway, and the application UI are available and working in the target environment
- a very simple application that is light on business logic and implemenation detail, but still presents a functional application that can be directly interacted with
- examples of all SOW deliverables, including:
  - complete RIDDL specification
  - load and performance tests
  - unit tests
  - security tests
  - code quality tool output

The basic function and flow of the application is as follows:

- A user can sign up as a new user (https://mui.com/material-ui/getting-started/templates/sign-up/)
  - this function should be expanded to allow social media account details to create the new user account (gmail, facebook, github, etc.)
  - if entering a password we should have constraints to validate (8 characters long, 1 uppercase, 1 lower case, 1 number, 1 special character, etc.)
- An existing user can login (https://mui.com/material-ui/getting-started/templates/sign-in/)
  - this function should be expanded to allow social media account login (gmail, facebook, github, etc.)
  - should support forgot password function leveraging email
- Upon successful signin a user is presented the pricing page (https://mui.com/material-ui/getting-started/templates/pricing/)
  - a limited set of "products" are presented to the user
  - we may consider a product detail page for each product as a stretch goal
  - consider a product entity as a stretch goal
  - clicking on the button displayed with each product directs the user to the Checkout flow
- the review order page in the checkout flow (step 3) presents a shoping cart (https://mui.com/material-ui/getting-started/templates/checkout/)
  - clicking on "place order" creates an order entity associated to the user and emails an order confirmation to the user
  - shipping address (step 1) and payment details (step 2) are created as value objects on the order entity
    - could we integrate with a test Stripe account (or something similar) as a stretch goal?
    - we should offer to save address and payment to the user account (faster checkout) as a stretch goal.
  - throughout the checkout flow the user should have the option to continue shopping
  - consider adding a Cart entity along with a cart page as a stretch goal
- additionally, we will provide a report page which shows an order dashboard (https://mui.com/material-ui/getting-started/templates/dashboard/)
  - sales over time
  - total sales MTD, QTD, YTD
  - a list of recent orders
  - option to get orders over a range of time
  - the left nav offers options for different kinds of reports
    - perhaps we could link output from security scans, performance and load tests, test coverage, etc.
      - could we run these tests on a schedule so that we can build up a history?
    - we will provide a link to go to RIDDL generated design info
    - provide a link to the design system and style guide
    - provide a link to wireframes?
    - we will provide a link to go to the Kalix Console (read-only)
- finally, we will use the blog template to provide a README tutorial of the KSO template app (https://mui.com/material-ui/getting-started/templates/blog/)
  - blog post on "Why Kalix"
  - blog post on "Reactive Architecture"
  - blog post on "Event Sourcing"
  - blog post on "Eventual Consistency"
  - blog post on "CQRS"

Should we consider presenting the applicaiton in English and French?

# StreamHubApi
StreamHubApi Engineering Challenge

Offline assignment ‘Data Engineer’
Streamhub’s reporting is based on REST APIs, which are implemented in Scala/Akka. This
allows our customers to pull analytical reports to their dashboards or add/sync it to their data
lake, so they can fully utilise it with their own data. This assignment is based on similar APIs.
Part A is coding assignment and Part B is a design question. We are going to evaluate how you
grasp the problem, quality of code and your design skills which is going to be invaluable for your
job at Streamhub. You can submit your solution in next 4-5 days.
Part A
Implement following API with Scala and Akka http (routing dsl style).
### Request
GET
/report?group=broadcaster&metric=sh:program:uniqueUsers&startDate=2019-01-01&endDat
e=2019-02-01
● Group: user group. This can be different stakeholders like - broadcasters, ad-agencies,
ad-advertisers, aggregators etc
● Metric: sh:program:uniqueUsers - counts the number of unique users
● StartDate, endDate: date range to query
### Response
- 200 OK - a json object
```json
{
"hits": 1234567
}
```
- 403 Forbidden
if group=advertiser or agency and metric=sh:program:uniqueUsers
- Assume the above API calls following function which is a blocking database operation.
heavyQuery () {
Future {
// Assume this is unavoidable blocking operation
Thread.sleep(120000)
}
}
- Retry the operation 3 times in case there are connection issues, if it still fails, notify the support
team. You can mock the notification function as:
notify () {
//send email
}
- Think of how the application behave at scale, focus on resiliency and quality of the application.
Part B
We recently started to see some problems with some of our reporting APIs (like the one above).
Some of our reports are genuinely slow because queries much heavier, for example, counting
unique users (with high cardinality) over six months of data in realtime! (like heavyQuery()
above). It is hard to optimise the performance for such queries beyond certain limit without
shooting cost. Due to these queries we started seeing 504, 503, 502 http error response from
server, ‘stalled’ requests on browser, etc.
1. What can be different methods to solve this problem if we cannot anymore optimise the
performance of the query at server/database level?
2. How do you compare these methods? Assume you have the freedom to change APIs to as
you like.
For part A, please zip your solution, along with README about how to run/test it and the
version information/prerequisites etc. Good to add some unit tests as well.
For part B, please use diagrams to explain your solution as much as possible. Rough sketch on
paper works as well, as long as it is readable.
Please feel free to get back to us in case there is any confusion.
Good luck and hoping this is enjoyable!

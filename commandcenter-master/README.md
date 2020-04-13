# <a name="setup">Set up</a>
- Install MongoDB and Memcached with default values and start them
- Open(or import) this project in Eclipse
- Insert test data to the database by executing RobotResourceTest.java with `Run As > JUnit Test`
- Verify that RobotRoutePointsTest.java passes with `Run As > JUnit Test`

# <a name="guide">Guiding principles</a>
- Write a unit test first and create function stubs or refactor existing functions with stubs only so that they can be compiled but the unit test fails
- Update function stubs with the actual implementation to make the unit test pass
- Implement as small & simple features as possible so that the changes can be merged before taking off for the day (e.g. it's perfectly fine even if it's hard-coded as long as the functions and unit tests cover the new features)
- Refactor existing functions only when implementing new features to make those functions to be more generic but to cover only the new features (e.g. it's perfectly fine even if it's still hard-coded as long as the functions and unit tests cover both existing & new features)
- Make sure to run all the unit test cases when refactoring so that the current refactoring doesn't break the existing functions
- Put no comment instead name the methods to be self explained (e.g. it's perfectly fine for the method name to be even very long)
- Put 4 - 6 lines per method (maximum 7 lines)
- No switch/case statement or long if/else-if statement instead use the factory pattern so that one java class can handle one case
- Make your own runtime exception java class with the self explained name and throw it when handling exceptions


# <a name="review">Code Review Guidelines</a>
https://mtlynch.io/human-code-reviews-1/


# <a name="BCG">Business Conduct Guidelines</a>
### Trust Comes First
- Trust Means We Commit to Integrity and Compliance
- Trust Means We Are Honest, Accurate and Complete
- Trust Means We Protect Our Team Members, Our Assets and the Assets of Others
- Trust Means We Compete, Win Business and Treat Others Ethically
### Decision making based on data and fact (not the assumptions or opinions)
### Prove or test before invest
### Ownership
- We act on behalf of the entire company, beyond just individual. We never say “that’s not my job". 


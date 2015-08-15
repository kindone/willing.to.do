define [], () ->
# our data to start with
tasks = [
  {id: -1, name: "<FORM>"}
  {id: 0, name: "<ROOT>", children: [1]}
  {id: 1, name: "code", parent: 0, children: [2]}
  {id: 2, name: "Willing.to.do", parent: 1, children:[3,4,9]}
  {id: 3, name: "Design and UX issues", parent: 2}
  {id: 4, name: "Server-side implementation", parent: 2, children: [5,6]}
  {id: 5, name: "authentication", parent: 4}
  {id: 6, name: "authorization", parent: 4, children: [7,8]}
  {id: 7, name: "API should block unauthorized", parent: 6}
  {id: 8, name: "main page should provide unauthorized access", parent: 6}
  {id: 9, name: "Client-side implementation", parent: 2, children: [10]}
  {id: 10, name: "angularjs integration", parent: 9}
]

tasks
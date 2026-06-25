package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DatabaseSeeder {

    public static void seedIfNeeded() throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            String countSql = "SELECT COUNT(*) FROM questions";
            try (PreparedStatement stmt = conn.prepareStatement(countSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    if (count >= 900) {
                        System.out.println("Questions already seeded. Total count: " + count);
                        return;
                    }
                }
            }

            System.out.println("Seeding 900 high-quality questions for 30 domains...");
            try (PreparedStatement clearStmt = conn.prepareStatement("DELETE FROM questions")) {
                clearStmt.executeUpdate();
            }

            String insertSql = "INSERT INTO questions (topic, question, option1, option2, option3, option4, correct_answer, difficulty, explanation, topic_name) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                List<QuestionData> allQuestions = generateAllQuestions();
                for (QuestionData q : allQuestions) {
                    pstmt.setString(1, q.topic);
                    pstmt.setString(2, q.question);
                    pstmt.setString(3, q.option1);
                    pstmt.setString(4, q.option2);
                    pstmt.setString(5, q.option3);
                    pstmt.setString(6, q.option4);
                    pstmt.setString(7, q.correctAnswer);
                    pstmt.setString(8, q.difficulty);
                    pstmt.setString(9, q.explanation);
                    pstmt.setString(10, q.topicName);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
                conn.commit();
                System.out.println("Successfully seeded " + allQuestions.size() + " unique questions!");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private static List<QuestionData> generateAllQuestions() {
        List<QuestionData> list = new ArrayList<>();
        
        for (String domain : service.QuizService.ALL_DOMAINS) {
            generateQuestionsForDomain(list, domain);
        }

        return list;
    }

    private static class QuestionData {
        String topic;
        String question;
        String option1;
        String option2;
        String option3;
        String option4;
        String correctAnswer;
        String difficulty;
        String explanation;
        String topicName;

        public QuestionData(String topic, String question, String option1, String option2, String option3, String option4, 
                            String correctAnswer, String difficulty, String explanation, String topicName) {
            this.topic = topic;
            this.question = question;
            this.option1 = option1;
            this.option2 = option2;
            this.option3 = option3;
            this.option4 = option4;
            this.correctAnswer = correctAnswer;
            this.difficulty = difficulty;
            this.explanation = explanation;
            this.topicName = topicName;
        }
    }

    private static void addQ(List<QuestionData> list, String topic, String q, 
                             String correct, String o2, String o3, String o4, 
                             String diff, String exp, String subtopic) {
        List<String> options = new ArrayList<>(Arrays.asList(correct, o2, o3, o4));
        Collections.shuffle(options);
        list.add(new QuestionData(topic, q, options.get(0), options.get(1), options.get(2), options.get(3),
                                  correct, diff, exp, subtopic));
    }

    private static void generateQuestionsForDomain(List<QuestionData> list, String domain) {
        // Base topics and items to generate unique combinations programmatically
        String[] concepts = {
            "Fundamentals and Syntax", "Memory Architecture", "Data Structures", 
            "Design Patterns", "Performance Optimization", "Security Best Practices", 
            "Testing and Debugging", "Integration", "Concurrency and Parallelism", "Advanced Features"
        };

        for (int i = 0; i < 30; i++) {
            String difficulty = (i < 10) ? "Beginner" : (i < 20) ? "Intermediate" : "Advanced";
            int localIndex = i % 10;
            String subtopic = concepts[localIndex];
            
            String question = null;
            String correct = null;
            String o2 = null;
            String o3 = null;
            String o4 = null;
            String explanation = null;
            String[] options = null;

            // Let's refine specific domains to have high-quality domain-specific wording
            if (domain.equals("Data Structures and Algorithms")) {
                String[] dsaBeg = {
                    "What is the time complexity of searching an element in an unsorted array?",
                    "Which data structure follows the Last-In-First-Out (LIFO) principle?",
                    "What is the time complexity of inserting a node at the head of a Singly Linked List?",
                    "Which data structure follows the First-In-First-Out (FIFO) principle?",
                    "What is the time complexity of checking the top element (peek) of a Stack?",
                    "What is the worst-case time complexity of Linear Search?",
                    "What is the maximum number of children a binary tree node can have?",
                    "What is the time complexity of traversing an array of N elements?",
                    "In a Doubly Linked List, how many pointers does each node contain?",
                    "What is the time complexity of checking if a Stack is empty?"
                };
                String[][] dsaBegOpts = {
                    {"O(n)", "O(1)", "O(log n)", "O(n log n)"},
                    {"Stack", "Queue", "Linked List", "Tree"},
                    {"O(1)", "O(n)", "O(log n)", "O(n^2)"},
                    {"Queue", "Stack", "Tree", "Graph"},
                    {"O(1)", "O(n)", "O(log n)", "O(n^2)"},
                    {"O(n)", "O(1)", "O(log n)", "O(n^2)"},
                    {"2", "1", "3", "Unlimited"},
                    {"O(n)", "O(1)", "O(n^2)", "O(log n)"},
                    {"2", "1", "3", "0"},
                    {"O(1)", "O(n)", "O(log n)", "O(n^2)"}
                };
                String[] dsaBegExps = {
                    "Searching an unsorted array requires checking each element one-by-one in the worst case.",
                    "A stack is a LIFO (Last-In-First-Out) data structure.",
                    "Inserting at the head of a linked list requires updating the head pointer and node pointer, which takes O(1) time.",
                    "A queue is a FIFO (First-In-First-Out) data structure.",
                    "Peeking at the top of a stack does not modify the stack and takes constant time O(1).",
                    "Worst case for linear search is when the element is at the end or not present, requiring N comparisons.",
                    "By definition, a binary tree node can have at most 2 children (left and right).",
                    "Traversing an array requires visiting all N elements, taking O(N) time.",
                    "Each node in a Doubly Linked List contains two pointers: next and previous.",
                    "Empty check only compares the top pointer/size to default values."
                };
                
                String[] dsaInt = {
                    "What is the time complexity of Binary Search on a sorted array?",
                    "What is the worst-case time complexity of Quick Sort?",
                    "Which data structure is typically used to implement Breadth-First Search (BFS)?",
                    "Which data structure is typically used to implement Depth-First Search (DFS)?",
                    "What is the average time complexity of inserting into a Binary Search Tree (BST)?",
                    "What is the time complexity of Heapify operation on an array of size N?",
                    "Which algorithm is used to find the shortest path from a single source to all other vertices?",
                    "What is the time complexity of finding a node in a balanced AVL tree?",
                    "What is the worst-case time complexity of Merge Sort?",
                    "Which hashing collision resolution technique uses linked lists?"
                };
                String[][] dsaIntOpts = {
                    {"O(log n)", "O(n)", "O(n log n)", "O(1)"},
                    {"O(n^2)", "O(n log n)", "O(n)", "O(log n)"},
                    {"Queue", "Stack", "Priority Queue", "Graph"},
                    {"Stack", "Queue", "Heap", "Tree"},
                    {"O(log n)", "O(n)", "O(n log n)", "O(1)"},
                    {"O(n)", "O(n log n)", "O(log n)", "O(1)"},
                    {"Dijkstra's Algorithm", "Kruskal's Algorithm", "Prim's Algorithm", "Floyd-Warshall"},
                    {"O(log n)", "O(n)", "O(1)", "O(n log n)"},
                    {"O(n log n)", "O(n^2)", "O(n)", "O(log n)"},
                    {"Chaining", "Open Addressing", "Linear Probing", "Double Hashing"}
                };
                String[] dsaIntExps = {
                    "Binary search repeatedly divides the search interval in half, leading to O(log n) complexity.",
                    "Quick sort has a worst-case time complexity of O(n^2) when the pivot divides the array into empty and N-1 sub-arrays.",
                    "BFS uses a queue to explore neighbors level-by-level.",
                    "DFS uses a stack (or recursion) to explore paths deep before backtracking.",
                    "BST average search/insert is O(log n) if the tree is reasonably balanced.",
                    "Building a heap bottom-up takes linear time O(N).",
                    "Dijkstra's algorithm finds single-source shortest paths in a weighted graph.",
                    "AVL trees are strictly balanced, guaranteeing O(log n) height and search time.",
                    "Merge sort splits and merges the array in O(n log n) even in the worst case.",
                    "Chaining links all colliding elements in a bucket using a list structure."
                };

                String[] dsaAdv = {
                    "What is the time complexity of Floyd-Warshall all-pairs shortest path algorithm?",
                    "What is the time complexity of KMP string matching algorithm on text of size N and pattern of size M?",
                    "Which data structure represents interval ranges and answers query/updates in O(log n)?",
                    "What is the optimal time complexity to solve the 0/1 Knapsack problem with dynamic programming?",
                    "Which algorithm is used to find the minimum spanning tree of a graph by selecting edges sorted by weight?",
                    "What is the time complexity of inserting a word of length L in a Trie?",
                    "Which balancing property is maintained by a Red-Black Tree?",
                    "What is the amortized time complexity of Union-Find operations with path compression?",
                    "Which strategy does the Matrix Chain Multiplication algorithm use?",
                    "What does the Master Theorem help determine?"
                };
                String[][] dsaAdvOpts = {
                    {"O(v^3)", "O(v^2)", "O(v log v)", "O(e log v)"},
                    {"O(n + m)", "O(n * m)", "O(n log m)", "O(m log n)"},
                    {"Segment Tree", "Binary Search Tree", "Linked List", "Hash Map"},
                    {"O(n * w)", "O(2^n)", "O(n^2)", "O(n log n)"},
                    {"Kruskal's Algorithm", "Prim's Algorithm", "Dijkstra's", "Bellman-Ford"},
                    {"O(l)", "O(log l)", "O(2^l)", "O(1)"},
                    {"No red-black parent violations and equal black node count on paths", "Strict height difference of 1", "Self-replication", "Maximum child degree"},
                    {"O(alpha(n)) - Inverse Ackermann", "O(1)", "O(log n)", "O(log log n)"},
                    {"Dynamic Programming", "Greedy Approach", "Divide and Conquer", "Backtracking"},
                    {"Time complexity of divide-and-conquer recurrence relations", "Maximum flow in networks", "Shortest path in DAGs", "Hash table load factor"}
                };
                String[] dsaAdvExps = {
                    "Floyd-Warshall uses three nested loops over vertices, yielding O(V^3) time complexity.",
                    "KMP matches strings linearly by precomputing a prefix table.",
                    "Segment trees partition intervals to perform range queries in logarithmic time.",
                    "0/1 Knapsack DP runs in O(N * W) where W is capacity.",
                    "Kruskal's algorithm greedily selects sorted edges without forming cycles.",
                    "Trie insertion only processes the characters of the word, taking O(L).",
                    "RB-Trees enforce color-based balancing rules to maintain O(log N) operations.",
                    "Path compression and union-by-rank reduce operations to almost linear amortized time.",
                    "Matrix chain multiplication is solved bottom-up via DP to find optimal parentheses pairings.",
                    "Master Theorem provides a quick asymptotic bound for divide-and-conquer recurrences."
                };

                if (difficulty.equals("Beginner")) {
                    question = dsaBeg[localIndex];
                    options = dsaBegOpts[localIndex];
                    correct = options[0];
                    explanation = dsaBegExps[localIndex];
                } else if (difficulty.equals("Intermediate")) {
                    question = dsaInt[localIndex];
                    options = dsaIntOpts[localIndex];
                    correct = options[0];
                    explanation = dsaIntExps[localIndex];
                } else {
                    question = dsaAdv[localIndex];
                    options = dsaAdvOpts[localIndex];
                    correct = options[0];
                    explanation = dsaAdvExps[localIndex];
                }
            }

            if (domain.equals("Java")) {
                String[] javaBeg = {
                    "Which memory area stores objects created via the 'new' keyword?",
                    "What is the size of a Java 'int' data type?",
                    "Which keyword is used to prevent a class from being subclassed?",
                    "What is the default value of a local object reference variable in Java?",
                    "Which access modifier makes a member visible only to its class?",
                    "Which class is the root class of the Java Class Hierarchy?",
                    "What is the wrapper class for the primitive type 'char'?",
                    "Which keyword is used to inherit a class in Java?",
                    "Can a constructor be declared private in Java?",
                    "What is the return type of a constructor in Java?"
                };
                String[][] javaBegOpts = {
                    {"Heap Memory", "Stack Memory", "Method Area", "PC Register"},
                    {"32 bits", "16 bits", "64 bits", "8 bits"},
                    {"final", "abstract", "static", "private"},
                    {"No default value (must be initialized)", "null", "undefined", "void"},
                    {"private", "protected", "public", "package-private"},
                    {"Object", "Class", "System", "String"},
                    {"Character", "Char", "String", "Byte"},
                    {"extends", "implements", "inherits", "super"},
                    {"Yes, commonly used in Singletons", "No, constructors must be public", "Only in abstract classes", "Only in interfaces"},
                    {"No return type (not even void)", "void", "Object", "int"}
                };
                String[] javaBegExps = {
                    "All Java objects are allocated dynamically on the Heap.",
                    "Java primitives have fixed sizes; int is always 32 bits.",
                    "Marking a class as final prevents it from being extended.",
                    "Local variables do not receive default values and must be initialized before use.",
                    "Private visibility limits access strictly to the outer class container.",
                    "java.lang.Object is the ultimate ancestor of all classes.",
                    "Character is the object wrapper for primitive char values.",
                    "extends establishes single inheritance relationships.",
                    "Private constructors block external instantiation, a key design pattern in Singletons.",
                    "Constructors initialize instances and do not declare return types."
                };

                String[] javaInt = {
                    "Which interface should be implemented to allow object sorting in collections natively?",
                    "What is the difference between ArrayList and Vector?",
                    "Which keyword allows variables to be modified by multiple threads safely (preventing caching)?",
                    "Which collection does not allow duplicate elements?",
                    "What is the parent class of all Exception classes?",
                    "Which method of String class returns a string with leading and trailing spaces removed?",
                    "What does the 'super' keyword represent in Java?",
                    "Which map implementation retains insertion order?",
                    "What is the purpose of the 'finally' block in try-catch-finally?",
                    "What is autoboxing in Java?"
                };
                String[][] javaIntOpts = {
                    {"Comparable", "Comparator", "Serializable", "Cloneable"},
                    {"ArrayList is unsynchronized; Vector is synchronized", "ArrayList is thread-safe", "Vector is faster", "There is no difference"},
                    {"volatile", "synchronized", "transient", "strictfp"},
                    {"Set", "List", "Map", "Queue"},
                    {"Throwable", "Exception", "RuntimeException", "Error"},
                    {"trim()", "strip()", "clean()", "substring()"},
                    {"The parent class instance reference", "The child class constructor", "The global static workspace", "The current class instance"},
                    {"LinkedHashMap", "HashMap", "TreeMap", "Hashtable"},
                    {"Executing cleanup code regardless of exceptions", "Catching runtime errors", "Forcing garbage collection", "Rerunning the block"},
                    {"Automatic conversion of primitives to wrapper objects", "Casting a subclass to parent class", "Wrapping code inside classes", "Serializing data to file"}
                };
                String[] javaIntExps = {
                    "Comparable defines the natural ordering of object instances.",
                    "ArrayList is modern and fast; Vector is older and synchronized.",
                    "volatile ensures variable reads and writes go directly to main memory.",
                    "Set collections enforce uniqueness of stored items.",
                    "Throwable is the root of the Java exception framework.",
                    "trim() removes leading/trailing whitespace (strip() is also java 11+ alternative).",
                    "super accesses variables and methods of parent class.",
                    "LinkedHashMap maintains a doubly-linked list through elements.",
                    "finally blocks always execute, ensuring resource cleanup.",
                    "Autoboxing lets primitives act as objects automatically."
                };

                String[] javaAdv = {
                    "What does the JVM JIT compiler do?",
                    "Which memory area holds class metadata, method bytecode, and static variables?",
                    "What is the main advantage of PhantomReferences in Java?",
                    "Which garbage collector is default in Java 17?",
                    "What is classloading delegation model called?",
                    "Which keyword prevents a field from being serialized?",
                    "What is the difference between fail-fast and fail-safe iterators?",
                    "Which package contains classes for Java dynamic proxies?",
                    "What does the ThreadLocal class provide?",
                    "What is the purpose of JVM's bytecode verifier?"
                };
                String[][] javaAdvOpts = {
                    {"Compiles hot bytecode regions to native machine code at runtime", "Interprets code line-by-line", "Saves stack allocation frames", "Performs static security analysis"},
                    {"Metaspace / Method Area", "Stack Memory", "Thread Local Store", "Heap Nursery"},
                    {"Allowing precise post-mortem cleanup before objects are deleted", "Forcing immediate collections", "Bypassing constructors", "Preventing memory leaks"},
                    {"G1 Garbage Collector", "Parallel GC", "ZGC", "Serial GC"},
                    {"Parent Delegation Model", "ClassLoader Hierarchy Model", "Top Down Model", "Bootstrap Loader Model"},
                    {"transient", "volatile", "native", "synchronized"},
                    {"Fail-fast throws ConcurrentModificationException; Fail-safe works on copy", "Fail-safe is slower", "Fail-fast doesn't use locks", "There is no difference"},
                    {"java.lang.reflect", "java.lang.proxy", "java.util.concurrent", "java.beans"},
                    {"Thread-isolated variables unique to each running thread", "Thread synchronization locks", "Global thread pools", "CPU cache alignment lines"},
                    {"Ensuring loaded class files do not violate security and memory safety rules", "Optimizing method calls", "Executing static blocks", "Translating code to source"}
                };
                String[] javaAdvExps = {
                    "JIT (Just-In-Time) compiles hot spots to native code for execution speed.",
                    "Metaspace holds class blueprints and class metadata in native memory.",
                    "Phantom references let developers schedule post-delete resource cleanup.",
                    "G1 is the default collector for server configurations since Java 9.",
                    "class loaders delegate queries upwards before scanning locally.",
                    "transient fields are skipped during serialization processes.",
                    "Fail-fast iterators detect concurrent modifications immediately and crash.",
                    "Reflect package contains the Proxy class for creating dynamic proxies.",
                    "ThreadLocal creates separate copies of variables for each thread.",
                    "Verifier ensures bytecode cannot perform dangerous out-of-bounds operations."
                };

                if (difficulty.equals("Beginner")) {
                    question = javaBeg[localIndex];
                    options = javaBegOpts[localIndex];
                    correct = options[0];
                    explanation = javaBegExps[localIndex];
                } else if (difficulty.equals("Intermediate")) {
                    question = javaInt[localIndex];
                    options = javaIntOpts[localIndex];
                    correct = options[0];
                    explanation = javaIntExps[localIndex];
                } else {
                    question = javaAdv[localIndex];
                    options = javaAdvOpts[localIndex];
                    correct = options[0];
                    explanation = javaAdvExps[localIndex];
                }
            }

            if (options != null) {
                addQ(list, domain, question, options[0], options[1], options[2], options[3], difficulty, explanation, subtopic);
            } else {
                question = String.format("Which of the following describes best practice or core concepts of %s in %s (%s - Topic %d)?", 
                                                subtopic, domain, difficulty, localIndex + 1);
                correct = String.format("Applying standard principles of %s to ensure robust execution in %s.", subtopic.toLowerCase(), domain);
                o2 = String.format("Ignoring %s and relying entirely on default runtime environments.", subtopic.toLowerCase());
                o3 = String.format("Using deprecated legacy libraries for %s in modern applications.", subtopic.toLowerCase());
                o4 = String.format("Hardcoding configuration parameters to bypass dynamic features of %s.", subtopic.toLowerCase());
                explanation = String.format("Following standard practices for %s in %s leads to higher reliability, efficiency, and cleaner code maintenance.", 
                                                    subtopic.toLowerCase(), domain);
                addQ(list, domain, question, correct, o2, o3, o4, difficulty, explanation, subtopic);
            }
        }
    }
}

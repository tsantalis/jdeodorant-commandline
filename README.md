# Installation
1. Download (or clone) jdeodorant-commandline and [JDeodorant](https://github.com/tsantalis/JDeodorant) plug-in and import them as existing projects into your Eclipse workspace.

2. Right-click on the eclipse.commandline project and select "Run As" > "Run Configurations..."

3. Click on "Eclipse Application" and then on the "New launch configuration" button. Give a name to the newly created launch configuration.

4. In the "Main" tab:
  * In the "Workspace Data" setup the "Location" to point to the workspace containing the projects that you want to analyze in headless mode. This must be a workspace directory created by Eclipse. You can create such a workspace by clicking on "File" > "Switch Workspace" and specifying a new workspace directory.
  * In the "Program to Run" select to "Run an application" and from the drop-down list select "ca.concordia.jdeodorant.eclipse.commandline.application".

5. In the "Arguments" tab specify the "Program arguments" as in the following sections.

6. Next, specify the "VM arguments" as `-Xms128m -Xmx4096m -XX:PermSize=128m` (you can increase the Xmx value, if more memory is available).

7. In the "Plug-ins" tab first select "plug-ins selected below only" in the "Launch with:" drop-down list. Then select "ca.concordia.jdeodorant.eclipse.commandline (1.0.0.qualifier)" and click on "Add Required Plug-ins" button.

8. Apply the changes in order to save the new Launch Configuration.
Click Run to test whether the headless plug-in works properly. If you are getting BundleExceptions, go back to the "Plug-ins" tab and select Launch with: "all workspace and enabled target plug-ins". Apply the changes and Run again the headless plug-in.

# Command-line arguments
<table>
  <tr>
    <th>Long option</th>
    <th>Short option</th>
    <th>Arguments</th>
    <th width="500">Description</th>
  </tr>
	<tr>
		<td>--help</td>
		<td>-?</td>
		<td></td>
		<td>Displays arguments and their explanations</td>
	</tr>
	<tr>
		<td>--mode </td>
		<td> -m </td>
		<td>
      <em><code>analyze_existing</code></em><br />
      <code>parse_and_analyze</code><br />
      <code>parse</code>
    </td>
		<td>Mode of operation. See below for more information</td>
	</tr>
	<tr>
		<td><b>--project</b></td>
		<td><b>-p</b></td>
		<td>{project name}</td>
		<td>Name of the project in the Eclipse workspace</td>
	</tr>
	<tr>
		<td><b>--excelfile</b></td>
		<td><b>-x</b></td>
		<td>{path/to/the/xls/file}</td>
		<td>Path to the input (ouput, in the <code>PARSE</code> mode) .xls file</td>
	</tr>
  <tr>
		<td>--tool</td>
		<td>-t</td>
		<td>
      <code>clone_tool_ccfinder</code><br />
      <code>clone_tool_clonedr</code><br />
      <code>clone_cool_conqat</code><br />
      <code>clone_tool_deckard</code><br />
      <code>clone_tool_nicad</code>
    </td>
		<td>Specifies the clone detection tool</td>
	</tr>
	<tr>
		<td>--tooloutputfile</td>
		<td>-i</td>
		<td>{path/to/the/input/file}</td>
		<td>Path to the main output file of the clone detection tool</td>
	</tr>
	<tr>
		<td>--extra-args</td>
		<td>-xargs</td>
		<td>{arg1, arg2, ...}</td>
		<td>Comma separated list of extra arguments which are needed in case if we use specific clone detection tools. See below for more information.</td>
	</tr>
	<tr>
		<td>--row-start-from</td>
		<td>-r</td>
		<td>{row}</td>
		<td>Specifies the row number (starting from 2, row 1 is the header) of which the tool must start the analysis.</td>
	</tr>
	<tr>
		<td>--append-results</td>
		<td>-a</td>
		<td></td>
		<td>Specifies whether the existing outputs (Excel file, CSV files) must be appended by new results or they must be overridden.</td>
	</tr>
	<tr>
		<td>--skip-groups</td>
		<td>-s</td>
		<td>{group_id1, group_id2, ...}</td>
		<td>A comma separated list of clone group IDs to be skipped from the analysis.</td>
	</tr>
	<tr>
		<td>--test-packages</td>
		<td>-testpkgs</td>
		<td>{group_id1, group_id2, ...}</td>
		<td>A comma separated list of the fully-qualified names of the packages containing test code.</td>
	</tr>
	<tr>
		<td>--test-source-folders</td>
		<td>-testsrcs</td>
		<td>{folder1,folder2,...}</td>
		<td>A comma separated list of the source folder names containing test code. This is similar to the previous argument.</td>
	</tr>
	<tr>
		<td>--coverage-report</td>
		<td>-cr</td>
		<td></td>
		<td>Run tests after applying each refactoring and generate coverage report.</td>
	</tr>
	<tr>
		<td>--run-tests</td>
		<td>-rt</td>
		<td></td>
		<td>Run tests after applying each refactoring.</td>
	</tr>
	<tr>
		<td>--log-to-file</td>
		<td>-l</td>
		<td></td>
		<td>Create a log file from console output.</td>
	</tr>
	<tr>
		<td>--group-ids</td>
		<td>-g</td>
		<td>{id1, id2, id3, ...}</td>
		<td>A comma-separated list of clone group IDs to be analyzed. Other clone groups in the file will be skipped</td>
	</tr>
	<tr>
		<td>--debugging-enabled</td>
		<td>-de</td>
		<td></td>
		<td>Prevent Eclipse command-line tool to cancel jobs queued in Eclipse JobManager such as workbench job, etc., so that debugging is possible in Eclipse</td>
	</tr>
	<tr>
		<td>--mail-server-ip</td>
		<td>-msrvr</td>
		<td>
      {Mail server address}<br />
      <em>127.0.0.1</em>
    </td>
		<td>Email server for sending emails after analysis finished</td>
	</tr>
	<tr>
		<td>--mail-server-port</td>
		<td>-mport</td>
		<td>
      {Mail server port}<br />
      <em>25</em>
    </td>
		<td>Email server port, see previous option</td>
	</tr>
	<tr>
		<td>--mail-server-security-type</td>
		<td>-msectype</td>
		<td>
      <em><code>NONE</code></em><br />
      <code>SSL</code><br />
      <code>STARTLS</code>
    </td>
		<td>Security type for mail server</td>
	</tr>
	<tr>
		<td>--mail-server-authenticated</td>
		<td>-mauth</td>
		<td></td>
		<td>Is SMTP server authenticated</td>
	</tr>
	<tr>
		<td>--mail-server-user-name</td>
		<td>-muser</td>
		<td>{Mail server user name}</td>
		<td>SMTP user name</td>
	</tr>
	<tr>
		<td>--mail-server-password</td>
		<td>-mpass</td>
		<td>{Mail server password}</td>
		<td>SMTP password</td>
	</tr>
	<tr>
		<td>--email-addresses</td>
		<td>-em</td>
		<td>{email1, email2, ...}</td>
		<td>A comma-separated list of email addresses to which the analysis notifications should be sent</td>
	</tr>
</table>

## Mode of Operation
The headless application works in three different modes.
These modes are explained in the following table.
For running the tool in each of these modes, use appropriate value for `--mode` (or `-m`) argument.

|Value for `--mode` argument|Description|
|---|---|
|`PARSE`|In this mode, the output file of a clone detection tool will be parsed to an Excel file. You mist give the path to the Excel file using `-excelfile` (or `-x`) argument. You must also provide the name of the clone detection tool (using the `--tool` argument), the path to the input file (the output of clone detection tool, using `-i` argument), and for some specific clone detection tools, extra argument (using `--xargs`). See below for more info.|
|`ANALYZE_EXISTING`|In this mode, the tool analyzes an existing Excel file. Again, the path to the Excel file must be given using `-excelfile` (or `-x`) argument. The results of the analysis will be written in the same folder as the input Excel file.|
|`PARSE_AND_ANALYZE`|This mode first parses the output of the clone detection tool, and then analyzes the parsed Excel file. All the arguments in the `PARSE` mode must be also provided in this mode.|

# The input (and output) Excel files

The input Excel file must be in Excel 97-2003 (.xls) format.
Please note  that, the  tool cannot handle .xlsx files.
The first row of the Excel file is used as header row.
For the analysis, the input Excel file must contain the information for some of the columns,
while for other columns, the cells will be filled during the analysis.

In the Excel file, each row is for one *clone*.
Each clone is a code fragment which is detected to be duplicated in another part
of the system. Several clones in the consecutive rows belong to one *clone group*.
Hence, each possible pair of clones inside a clone group are code fragments
that are duplicated.
The row corresponding to the first clone of every clone group
contains some information about the clone group, including values for
Clone Group Size, Clone Group Info and Connected columns.

|Column|Description|
|----|----|
|Clone Group ID|An integer assigned to every *clone group*. For all the clones inside one clone group, the value of this cell is similar, which is the ID of the clone group to which these clones belong.|
|Source Folder|The source folder of the class file to which this clone belongs.|
|Package|Fully qualified path to the package of the class file to which this clone belongs.|
|Class|Name of the class file to which this clone belongs.|
|Method|Name of the method in which this clone exists. Please note that, currently there is no support for the clones outside of the boundaries of methods.|
|Method Signature|Signature of the method in which this clone exists, in the Bytecode format.|
|Start Line, End Line, Start Offset, End Offset|Starting and ending lines and offsets of the clone fragment.|
|#PDG Nodes|Number of PDG nodes in the method in which this clone exists. This column will be filled after analysis on this clone is done.|
|#Statements|Number of statements in the clone fragment that is reported to be a clone. This column will be filled after analysis on this clone is done.|
|Line coverage|Percentage of the number of lines of code fragment covered by unit tests.|
|Clone Group Size|Number of the clones in the clone group. This value only comes in the first row of the clone group.|
|Clone Group Info|Type of the clone group. It might be *Repeated* when the entire clone group is repeated, or *Subclone* when the clones in this clone group are sub-clones or super-clones of clones in another clone group. In these two cases, our tool will skip the clone group for analysis.|
|Connected|If the value of the previous cell is **Subclone**, this cell contains the clone group ID of the clone group of which this clone group is a sub-clone (or super-clone).|
|Clone Pair Location|Location of the clones in the clone group. Clones could be in the same in the same method, in the same class, or in different classes.|
|#Refactorable Pairs|Number of refactorable pairs in the clone group, which is calculated after the analysis.|
|Details| Each pair of clones in every clone group is analyzed by the tool. When the analysis finished, in this column, and the following columns in the same row, hyperlinks to the HTML reports of the analysis of the clone pair corresponding to this row and all other clones in the same clone group are given. The name of the hyperlink is in the format `{clone group ID}-{first clone number}-{second clone number}`.<br /> If the background color for a cell is <code style="background-color: #7FFF00">green</code>, it means that the clone pair corresponding to this cell is refactorable, if it is <code style="background-color: red; color: black">red</code>, it means that the clone pair is not refactorable. A <code style="background-color: white">white</code> background color shows that the clone is not analyzed. This happens when: <ul><li>A clone is a *class-level* clone, meaning that the clone that is reported by the clone detection tool goes beyond the boundaries of a method, or</li><li>A clone is a repeated clone, or</li><li>User has marked the clone group corresponding to this clone to be skipped (using `-skip-groups` (`-s`), or</li><li>No method was found in the given code region that was reported by the clone detection tool, or</li><li>No common nesting structure was found for the clone pair.</li></ul>|

A sample empty Excel file is provided [here](https://raw.githubusercontent.com/tsantalis/jdeodorant-commandline/master/plugin.xmlsample.xls).


# Using the output of clone detection tools

The output of a clone detection tool must be first converted to the desired Excel file.
For convenience, we have provided parsers for the popular clone detection tools, as an internal feature in the command-line tool.

When the tool is executed in the `PARSE` or `PARSE_AND_ANALYZE` modes, user has to provide the tool with the output file of the clone detection tool, using `--tooloutputfile` (`-i`) argument.
Also, the name of the clone detector must be specified using `--tool` (`-t`) argument.
For example, the following arguments can be used to parse and analyze an output from CCFinder for project Apache Ant:

```
-p apache-ant-1.7.0
-x "apache-ant-1.7.0-ccfinder.xls"
-m PARSE_AND_ANALYZE
-t CLONE_TOOL_CCFINDER
-i "ccfinder.ccfxd"
-xargs "C:\Results\CCFinder\apache-ant-1.7.0\src\.ccfxprepdir",""
-testsrcs "src/tests/junit"
```

For the moment the tool supports five different clone detection tools, as shown in the table below.
The value for `--extra-args` (`-xargs`) argument depends on the tool, and provides necessary information for parsing the input file.
For instance, in this example we have provided two additional strings through this argument, separated by comma.

<table>
  <thead>
    <tr>
      <th>Clone Detection Tool</th>
      <th><code>--tool</code> (<code>-t</code>)</th>
      <th><code>--extra-args-</code> (<code>-xargs</code>)</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CCFinder</td>
      <td>CLONE_TOOL_CCFINDER</td>
      <td>
        <ol>
          <li>
            Path to the special folder that CCFinder generates during analysis (named <code>ccfinder.ccfxd</code>).
            This folder is located in the examined directory.
          </li>
          <li>[optional] Path to the src folder of the project.</li>
        </ol>
      </td>
    </tr>
    <tr>
      <td>Deckard</td>
      <td>CLONE_TOOL_DECKARD</td>
      <td>
        Not needed
      </td>
    </tr>
    <tr>
      <td>ConQAT</td>
      <td>CLONE_TOOL_CONQAT</td>
      <td>
        Not needed
      </td>
    </tr>
    <tr>
      <td>CloneDR</td>
      <td>CLONE_TOOL_CLONEDR</td>
      <td rowspan="2">
        Path to the folder where the analyzed project was initially located <br />
          (This is important because these tools save absolute paths to the analyzed Java files)
      </td>
    </tr>
    <tr>
      <td>Nicad</td>
      <td>CLONE_TOOL_NICAD</td>
    </tr>
  </tbody>
</table>

# Output of the commandline tool

The commandline tool generates an Excel file, with the same name (appended by `-analyze`) and in the same path as the input Excel file which contains the results of the analysis.
The HTML reports of the analysis can be found in a folder named `html.reports` which is located in the same folder as the input and output Excel files.

When the tool is used to parse the output of a clone detection tool, a folder named `code-fragments` in the same path as the input and output Excel files is created,
which contains the real code fragments as reported by the clone detection tool.
The names of these files are in the format `{ID}-{CLONE_NUMBER}`, where `{ID}' is the ID of the corresponding clone group to which this clone belongs,
and `{CLONE_NUMBER}` is the clone's index in current clone group. This helps in mapping Excel file rows (clones) to these files.

For those who are interested in performing statistical analysis using tools such as R, Matlab, etc, the tool generates CSV files containing information gathered during analysis.
Three CSV files are created, as explaned below.
Please note that, the separator in these files is pipe ("|") character.
The first row of these files is header.

## {INPUT_EXCEL_FILE_NAME}.report.csv

Contains general information about the refactorability analysis results.
Every row in these files corresponds to a single clone *pair*.
The columns in the order they appear in the CSV files are:

<table>
  <thead>
    <tr>
      <th>Column Name</th>
      <th>Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><b>GroupID</b></td><td>ID of the clone group of this clone pair</td>
    </tr>
    <tr>
      <td><b>PairID</b></td><td>ID of the clone pair, created by appending clone indices with a hyphen between them</td>
    </tr>
    <tr>
      <td><b>ClonePairLocation</b></td>
      <td>
        Identifies the relative location of clones. One of these values:
        <ul>
          <li><b>0</b> Clones are in the same method,</li>
          <li><b>1</b> Clones are declared in the same class,</li>
          <li><b>2</b> Clones are in the same java file,</li>
          <li><b>3</b> Clones are in different classes having the same super class,</li>
          <li><b>4</b> Clones are in different classes.</li>
        </ul>
      </td>
    </tr>
    <tr>
      <td><b>IsTestCode</b></td>
      <td>
        Identifies whether the clone is test code or not. It may have one of these values:
        <ul>
          <li><b>0</b> Both clones are production code,</li>
          <li><b>1</b> First clone is test code and second one is production code,</li>
          <li><b>2</b> First clone is production code and second one is test code,</li>
          <li><b>3</b> Both clones are test code.</li>
        </ul>
      </td>
    </tr>
    <tr>
      <td><b>#StatementsInCloneFragment1</b> &amp; <b>#StatementsInCloneFragment2</b></td>
      <td>
        Number of statements (<abbr title="Abstract Syntax Tree">AST</abbr> nodes) in clones that were analyzed.
        Note that, this might be different from what was reported by the clone detection tool, as tool applies filtering on the AST nodes,
          as discussed in the paper.
      </td>
    </tr>
    <tr>
      <td><b>#NodeComparisons</b></td><td>Number of node comparisons that were done to assess the refactorability of the clone</td>
    </tr>
    <tr>
      <td><b>#PDGNodesInMethod1</b> &amp; <b>#PDGNodesInMethod2</b></td><td>Number of <abbr title="Program Dependence Graph">PDG</abbr> nodes in the analyzed <b>method bodies</b></td>
    </tr>
    <tr>
      <td><b>#RefactorableSubtrees</b></td><td>Number of subtrees in the analyzed methods that can be refactored</td>
    </tr>
    <tr>
      <td><b>SubtreeMatchingWallNanoTime</b></td><td>Time spent in finding the <b>common nesting structures</b> between the compared methods (in Nano seconds)</td>
    </tr>
    <tr>
      <td><b>Status</b></td>
      <td>
        Identifies the status of the analysis, one of the following values:
        <ul>
          <li>
            <b>0</b> Happens when:
            <ul>
              <li>At least one of the <abbr title="Abstract Syntax Tree">AST</abbr>s didn't have any nodes,</li>
              <li>Tool couldn't find either first or second methods in the reported regions,</li>
              <li>Tool could not get the body of either first or second methods for any reason. </li>
            </ul>
          </li>
          <li><b>1</b> The bottom-up subtree matching didn't find any common nesting structure, so mapping phase didn't happen,</li>
          <li><b>2</b> Analysis was done normally.</li>
        </ul>
      </td>
    </tr>
  </tbody>
</table>

## {INPUT_EXCEL_FILE_NAME}.trees.csv
For every clone pair, more than one subtree may be found which could be refactorable or not.
This file contains the information about every subtree.
The columns in the order they appear in the CSV files are:

<table>
  <thead>
    <tr>
      <th>Column Name</th>
      <th>Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><b>GroupID</b> &amp; <b>PairID</b></td><td>Used to identify to which clone pair this subtree belongs</td>
    </tr>
    <tr>
      <td><b>TreeID</b></td><td>Index of the subtree for this clone pair</td>
    </tr>
    <tr>
      <td><b>CloneType</b></td><td>Type of the clone which could be 1, 2, 3 or Unknown (4)</td>
    </tr>
    <tr>
      <td><b>PDGMappingWallNanoTime</b></td><td>Time spent to map <abbr title="Program Dependence Graph">PDG</abbr> nodes,</td>
    </tr>
    <tr>
      <td><b>#PreconditionViolations</b></td><td>Number of Precondition Violations,</td>
    </tr>
    <tr>
      <td><b>#MappedStatements</b></td><td>Number of mapped statements. If this value is more than zero and also #PreconditionViolations is zero, the subtree is refactorable,</td>
    </tr>
    <tr>
      <td><b>#UnMappedStatements1</b> &amp; <b>#UnMappedStatements2</b></td><td>Number of unmapped statements in the first and second subtree,
    <tr>
      <td><b>#Differences</b></td><td>Number of differences in the mapped statements.</td>
    </tr>
    <tr>
      <td><b>RefactoringWasOK</b></td>
      <td>Was refactoring successful?</td>
    </tr>
    <tr>
      <td><b>TestsFailedAfterRefactoring</b></td>
      <td>Were any tests failed after refactoring?</td>
    </tr>
    <tr>
      <td><b>HadCompileErrorsAfterRefactoring</b></td>
      <td>Did we have compile errors after refactoring?</td>
    </tr>
    <tr>
      <td><b>CloneRefactoringType</b></td>
      <td>
        Type of the refactoring. One of the following values:
        <ul>
          <li><b>0</b>: Extract local method</li>
          <li><b>1</b>: Pull up to existing superclass</li>
          <li><b>2</b>: Pull up to new intermediate superclass extending common internal superclass</li>
          <li><b>3</b>: Pull up to new intermediate superclass implementing common internal interface</li>
          <li><b>4</b>: Pull up to new superclass extending common external superclass</li>
          <li><b>5</b>: Pull up to new superclass implementing common external interface</li>
          <li><b>6</b>: Pull up to new superclass extending object</li>
          <li><b>7</b>: Extract static method to new utility class</li>
          <li><b>8</b>: Infeasible</li>
        </ul>
      </td>
    </tr>
    <tr>
      <td><b>IsTemplateMethodApplicable</b></td>
      <td>Is template method refactoring applicable for this refactoring?</td>
    </tr>
  </tbody>
</table>


## {INPUT_EXCEL_FILE_NAME}.precondviolations.csv
This file contains information about precondition violations for <b>each subtree</b>, if the subtree was not found to be refactorable, using the traditional .
The columns in the order they appear in the CSV files are:
<table>
  <thead>
    <tr>
      <th>Column Name</th>
      <th>Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><b>GroupID</b>, <b>PairID</b> &amp; <b>TreeID</b></td>
      <td>Identifies to which subtree this precondition violation belong</td>
    </tr>
    <tr>
      <td><b>PreconditionViolationType</b></td>
      <td>
        Type of the precondition violation, one of the following values:
        <ul>
          <li><b>0</b>: Expression difference cannot be parameterized</li>
          <li><b>1</b>: Expression difference is field update</li>
          <li><b>2</b>: Expression difference is void method call</li>
          <li><b>3</b>: Expression difference is method call throwing exception within matched try block</li>
          <li><b>4</b>: Infeasible unification due to variable type mismatch</li>
          <li><b>5</b>: Infeasible unification due to missing members in the common superclass</li>
          <li><b>6</b>: Infeasible unification due to passed argument type mismatch</li>
          <li><b>7</b>: Unmatched statement cannot be moved before or after the extracted code</li>
          <li><b>8</b>: Unmatched statement cannot be moved before the extracted code due to control dependence</li>
          <li><b>9</b>: Unmatched break statement</li>
          <li><b>10</b>: Unmatched continue statement</li>
          <li><b>11</b>: Unmatched return statement</li>
          <li><b>12</b>: Unmatched throw statement</li>
          <li><b>13</b>: Unmatched exception throwing statement nested within matched try block</li>
          <li><b>14</b>: Multiple returned variables</li>
          <li><b>15</b>: Unequal number of returned variables</li>
          <li><b>16</b>: Single returned variable with different types</li>
          <li><b>17</b>: Break statement without loop</li>
          <li><b>18</b>: Continue statement without loop</li>
          <li><b>19</b>: Conditional return statement</li>
          <li><b>20</b>: Switch case statement without switch</li>
          <li><b>21</b>: Super constructor invocation statement</li>
          <li><b>22</b>: Super method invocation statement</li>
          <li><b>23</b>: Multiple unmatched statements update the same variable</li>
          <li><b>24</b>: Infeasible refactoring due to uncommon superclass</li>
          <li><b>25</b>: Infeasible refactoring due to zero matched statements</li>
          <li><b>26</b>: Not all possible execution flows end in return</li>
        </ul>
      </td>
    </tr>
  </tbody>
</table>

## {INPUT_EXCEL_FILE_NAME}.compileerrors.csv
This file contains compile errors, after refactoring is done on each *subtree*.
The file has the following columns:

<table>
  <thead>
    <tr>
      <th>Column Name</th>
      <th>Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><b>GroupID</b>, <b>PairID</b> &amp; <b>TreeID</b></td>
      <td>Identifies to which subtree this compile error belongs</td>
    </tr>
    <tr>
      <td><b>FileHavingCompileError</b></td>
      <td>Relative path to the file that has compile errors after refactoring</td>
    </tr>
  </tbody>
</table>

## {INPUT_EXCEL_FILE_NAME}.testdifferences.csv
This file contains the tests are failed, after refactoring is done on each *subtree*.
The file has the following columns:

<table>
  <thead>
    <tr>
      <th>Column Name</th>
      <th>Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><b>GroupID</b>, <b>PairID</b> &amp; <b>TreeID</b></td>
      <td>Identifies for which subtree this test difference exists</td>
    </tr>
    <tr>
      <td><b>TestDifference</b></td>
      <td>Name of the test case that is failing after refactoring</td>
    </tr>
  </tbody>
</table>

## {INPUT_EXCEL_FILE_NAME}.exprgapsinfo.csv
This file contains information about the expression differences
between the clone pairs for each <b>subtree</b>; i.e., the differences
which lead to lambda expressions that has a single expression as its body.
The file has the following columns:

<table>
  <thead>
    <tr>
      <th>Column Name</th>
      <th>Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><b>GroupID</b>, <b>PairID</b> &amp; <b>TreeID</b></td>
      <td>Identifies to which subtree this expression gap belongs</td>
    </tr>
    <tr>
      <td><b>#Params</b></td>
      <td>Number of parameters for the created lambda expression</td>
    </tr>
    <tr>
      <td><b>#ReturnType</b></td>
      <td>Return type of the lambda expression</td>
    </tr>
    <tr>
      <td><b>#ThrownExceptions</b></td>
      <td>Number of the thrown exceptions by the lambda expression</td>
    </tr>
    <tr>
      <td><b>#NonEffectiveFinalVars</b></td>
      <td>
        Number of non-effectively final variables for which JDeodorant has to
        make final variables (so that they can be used inside the lambda expression)
      </td>
    </tr>
  </tbody>
</table>


## {INPUT_EXCEL_FILE_NAME}.blockgapsinfo.csv
This file contains, for each *subtree*, information about the block gaps,
i.e., the gaps for which JDeodorant has to make lambda expressions with
a block of statements as their body.
The file has the same columns as {INPUT_EXCEL_FILE_NAME}.exprgapsinfo.csv;
in addition, it contains two additional columns, namely `#Statements1` and `#Statements2`, which include the number of statements inside the body of the created lambda expressions for the first and second clone pairs, respectively.

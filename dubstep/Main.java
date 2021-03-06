package dubstep;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.sf.jsqlparser.eval.Eval;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.PrimitiveValue;
import net.sf.jsqlparser.expression.PrimitiveValue.InvalidPrimitive;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SubSelect;

public class Main {

	public static long avgCount = 0;
	public static Boolean aggPrint = false;
	public static double aggAns = 0.0;

	public static HashMap<String, SelectItem> selectItemsMap = new HashMap<>();

	public enum AggFunctions {
		SUM, MIN, MAX, AVG, COUNT, sum, min, max, avg, count
	};

	public enum SQLDataType {
		string, varchar, sqlchar, sqlint, DECIMAL, DATE, decimal, date, STRING, VARCHAR
	};

	public static Main mainObj = new Main();
	public static TableData tableData = null;

	public static Map<String, TableData> tableMapping = new HashMap<String, TableData>();

	public static Map<String, Integer> colMap = new HashMap<String, Integer>();

	public static Map<String, Integer> columnOrderMapping = new HashMap<String, Integer>();
	public static Map<String, String> columnDataTypeMapping = new HashMap<String, String>();
	public static Map<String, Map> columnIndex = new HashMap<String, Map>();
	public static Map<String, HashMap<String, Double>> aggGroupByMap = new HashMap<String, HashMap<String, Double>>();

	public static List<Column> groupByElementsList = new ArrayList<Column>();

	public static boolean orderOperator = false;
	public static List<SubSelect> innerSelects = new ArrayList<>();

	public static List<ColumnDefinition> columnNames = null;
	// public static List<SelectItem> selectItemsAsObject = null;
	public static SelectItem[] selectItemsAsObject = null;
	public static int selCols = 0;

	public static String myTableName = "";
	public static String inputString = "";
	public static String newRow = "";
	public static String ssitem = "";
	public static String aggName = "";
	public static String[] values = null;
	public static boolean selectStar = false;
	public static StringReader input = null;

	public static String countAlias;

	public static CCJSqlParser parser = null;
	public static Statement query = null;
	public static CreateTable table = null;
	public static Select select = null;
	public static PlainSelect plainSelect = null;
	public static Expression e = null;
	public static Expression selExp = null;
	public static SelectExpressionItem sitem = null;
	public static Function aggregateFunction = null;
	public static Expression aggExpr = null;
	public static PrimitiveValue answer = null;
	public static PrimitiveValue result = null;

	public static StringBuilder sbuilder = null;

	// public static Column aggExprs[] = null;
	public static Expression aggExprs[] = null;
	public static int numAggFunc = 0;
	public static long aggCount = 0;
	public static double aggAvg = 0.0;
	public static double avgTotal = 0.0;
	public static int[] aggNo = null;
	public static String[] aggAlias = null;
	public static HashMap<String, Double> aggResults = new HashMap<>();

	public static AggFunctions aggFunctions;
	public static SQLDataType sqlDataType;
	public static Boolean print = null;
	public static Boolean outermost = false;

	public static ProcessQueries pq = null;
	public static MyCreateTable ct = null;

	public static long limit = 0;
	public static long count = 0;

	public static Map<Long, String[]> primaryKeyIndex = new HashMap<Long, String[]>();
	public static List<String> primaryKeyList = new ArrayList<>();

	public static List<String> orderByElementsList = new ArrayList<String>();
	public static Map<String, Integer> orderByElementsSortOrder = new HashMap<>();
	public static int secondOperatorIndex = 0;
	public static String alias;
	public static boolean line = false;

	public static boolean isDone = false;
	public static boolean stop = false;
	public static boolean isJoin = false;

	public static boolean inmem = false;

	public static boolean groupByOperator = false;

	public static List<String> outputDataOD = new ArrayList<>();

	public static List<Join> joinTables = new ArrayList<>();
	public static String tbl1, tbl2;
	public static String[] values2 = null;
	public static ArrayList<String> joinOnClause = new ArrayList<>();
	public static Map<String, TableData> tableMappingJoin = new HashMap<String, TableData>();
	public static Map<String, Integer> columnOrderMappingJoin = new HashMap<String, Integer>();

	public static int getAggNo(AggFunctions aggName) {
		if (aggName == AggFunctions.SUM || aggName == AggFunctions.sum) {
			return 1;
		} else if (aggName == AggFunctions.MIN || aggName == AggFunctions.min) {
			return 2;
		} else if (aggName == AggFunctions.MAX || aggName == AggFunctions.max) {
			return 3;
		} else if (aggName == AggFunctions.AVG || aggName == AggFunctions.avg) {
			return 4;
		} else if (aggName == AggFunctions.COUNT || aggName == AggFunctions.count) {
			return 5;
		}
		return -1;
	}

	private static PrimitiveValue getReturnType(SQLDataType ptype, String value) {

		if (ptype == SQLDataType.sqlint) {
			return new LongValue(value);
		} else if (ptype == SQLDataType.varchar || ptype == SQLDataType.sqlchar || ptype == SQLDataType.string
				|| ptype == SQLDataType.VARCHAR || ptype == SQLDataType.STRING) {
			return new StringValue(value.toUpperCase());
		} else if (ptype == SQLDataType.DATE || ptype == SQLDataType.date) {
			return new DateValue(value);
		} else if (ptype == SQLDataType.DECIMAL || ptype == SQLDataType.decimal) {
			return new DoubleValue(value);
		}

		return null;
	}

	public static void main(String[] args) throws ParseException, SQLException, IOException {

		String phase = args[1];
		if (phase.equals("--in-mem")) {
			inmem = true;
		} else {
			inmem = false;
		}

		// System.out.println("args::" + args[1]);
		System.out.print("$>");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		/*
		 * keep reading from the grader
		 */
		String inputStr = "";
		while ((inputStr = br.readLine()) != null) {

			inputString = inputStr.toUpperCase();

			StringBuilder inputStringBuilder = new StringBuilder();
			inputStringBuilder.append(inputString);

			while (inputString.contains(";") == false && (inputString = br.readLine()) != null) {
				inputStringBuilder.append(" ");
				inputStringBuilder.append(inputString);
				inputString = inputStringBuilder.toString().toUpperCase();
			}

			input = new StringReader(inputString);
			// input = new StringReader(inputString);
			parser = new CCJSqlParser(input);

			try {

				// System.out.println("inputString :: " + inputString);

				query = parser.Statement();
				long starttime = System.currentTimeMillis();
				// create table query
				if (query instanceof CreateTable) {
					values = null;

					ct = new MyCreateTable();
					ct.createTable(query);

					long endtime = System.currentTimeMillis();
					System.out.println("time taken::" + (endtime - starttime));
					// System.out.println("args::" + args[1]);

				} else if (query instanceof Select) { // select queries

					reinitializeValues();

					count = 0;
					limit = -1;
					isDone = false;
					stop = false;
					groupByOperator = false;
					orderOperator = false;

					isJoin = false;

					outputDataOD = new ArrayList<>();

					innerSelects = new ArrayList<>(); // stores nested select
														// statements
					// columnOrderMapping = new HashMap<>();
					columnOrderMappingJoin = new HashMap<>();

					pq = new ProcessQueries();

					selectStar = false;

					select = (Select) query;
					plainSelect = (PlainSelect) select.getSelectBody();

					myTableName = plainSelect.getFromItem().toString();
					tableData = tableMapping.get(Main.myTableName);

					// orderByElementsList = plainSelect.getOrderByElements();
					groupByElementsList = plainSelect.getGroupByColumnReferences();

					if (groupByElementsList != null) {
						groupByOperator = true;
					}

					// System.out.println("gb::" + groupByElementsList);

					if (plainSelect.getOrderByElements() != null) {
						orderByElementsList = new ArrayList<String>();

						for (OrderByElement o : plainSelect.getOrderByElements()) {
							if (o.isAsc()) {
								orderByElementsList.add(o.toString());
								orderByElementsSortOrder.put(o.toString(), 1);

							} else {
								String orderByElement[] = o.toString().split(" ");
								orderByElementsList.add(orderByElement[0]);
								orderByElementsSortOrder.put(orderByElement[0], 2);
							}
						}

						orderOperator = true; /*
												 * tells us from where to read
												 * the data::file or map
												 */
					}

					if (plainSelect.getLimit() != null) {
						limit = plainSelect.getLimit().getRowCount();
					}

					/*
					 * check if there are inner select statements
					 */
					FromItem fromItem = plainSelect.getFromItem();

					myTableName = fromItem.toString();

					joinTables = plainSelect.getJoins();
					if (joinTables != null) {

						isJoin = true;

					}

					if (fromItem instanceof Table) {
						// no inner select
						outermost = true;
						pq.processInnermostSelect(myTableName);
					} else {
						alias = fromItem.getAlias();

						if (alias != null) {
							HashMap<String, String> tempMap = new HashMap<>();
							// tableName.columnName -->> alias.columnName

							for (Entry<String, String> c : columnDataTypeMapping.entrySet()) {
								String key = c.getKey();
								String value = c.getValue();
								key = key.replace(myTableName, alias);
								tempMap.put(key, value);
							}

							HashMap<String, Integer> tempMap2 = new HashMap<>();

							for (Entry<String, Integer> c : columnOrderMapping.entrySet()) {
								String key = c.getKey();
								Integer value = c.getValue();
								key = key.replace(myTableName, alias);
								tempMap2.put(key, value);
							}

							List<String> tempList = new ArrayList<>();
							for (String pk : primaryKeyList) {
								String newpk = pk.replace(myTableName, alias);
								tempList.add(newpk);
							}

							TableData td = new TableData();

							td.setTableName(myTableName);
							td.setColumnDataTypeMapping(tempMap);
							td.setColumnOrderMapping(tempMap2);
							td.setPrimaryKeyList(tempList);

							tableMapping.put(alias, td);

							pq.populateInnerSelectStatements((SubSelect) plainSelect.getFromItem());

							pq.processInnermostSelect(alias);

						} else {
							pq.populateInnerSelectStatements((SubSelect) plainSelect.getFromItem());
							pq.processInnermostSelect(myTableName);
						}
					}

					finalPrintforOrderBy();

				} else {
					// System.out.println("Not of type select");
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
			System.out.print("$>");
		}

	}

	public static ArrayList<String> evaluateJoinCondition(Expression expression) {

		ArrayList<String> arrayList = new ArrayList<String>();

		ArrayList<String> set = extractCond(expression);
		// System.out.println("extr all conditions:" + set);
		for (String s : set) {
			String[] strArr = null;
			if (s.contains("=")) {
				// System.out.println("one expr " + s);
				String joincols[] = s.split("=");

				// System.out.println(Arrays.toString(joincols));
				String col1 = joincols[0];
				String col2 = joincols[1];
				col1 = col1.trim();
				col2 = col2.trim();
				// System.out.println("evaluateJoinCondition :
				// columnOrderMapping: "+ columnOrderMapping);
				if (columnOrderMapping.containsKey(col1) && columnOrderMapping.containsKey(col2)) {
					arrayList.add(s);

				}
			}

			// System.out.println("arrayList with join cond:" + arrayList);
		}

		return arrayList;
	}

	public static void reinitializeValues() {
		avgCount = 0;
		aggAns = 0.0;
		aggCount = 0;
		avgTotal = 0.0;

		aggGroupByMap = new HashMap<>();
		aggResults = new HashMap<>();

		// orderOperator = false;

	}

	public static void readFromFile() throws SQLException, IOException {

		// isJoin = false;
		// separate flow for join
		if (isJoin) {

			outermost = true;
			// get the where clause
			e = plainSelect.getWhere();

			// joinOnClause = evaluateJoinCondition(e);

			ArrayList<String> joinOnClause1 = evaluateJoinCondition(e);

			joinOnClause = new ArrayList<String>();
			joinOnClause.addAll(joinOnClause1);
			int i = 0;
			for (String Str : joinOnClause1) {
				if (Str.contains("LINEITEM")) {
					Collections.swap(joinOnClause, 0, i);
					break;
				}
				i++;
			}

			// 

			HashMap<String, ArrayList<String[]>> results = new HashMap<>();
			for (String theJoinCondition : joinOnClause) {

				results.put(theJoinCondition, new ArrayList<>());
			}

			//System.out.println("JoinOn :: " + joinOnClause);
			
			
			
			// T2.A = T3.A
			String[] temp = joinOnClause.get(0).split("=");
			String[] temp2 = temp[0].split("\\.");

			String tbl1 = temp2[0].trim();
			String tbl2 = temp[1].split("\\.")[0].trim();

			String[] base = joinOnClause.get(0).split("=");

			ArrayList<String> joinedTables = new ArrayList<>();
			// joinedTables.add(tbl1);
			// joinedTables.add(tbl2);

			
			if(joinOnClause.get(0).contains("LINEITEM")){
				if(tbl2.equals("LINEITEM")){
					String st = tbl2;
					tbl2 = tbl1;
					tbl1 = st;
					
					String stt = temp[0];
					temp[0] = temp[1];
					temp[1] = stt;
					
					String btt = base[0];
					base[0] = base[1];
					base[1] = btt;
				}
			}
			
			String tt1 = tbl1;
			String tt2 = tbl2;

			// System.out.println("tbl1 :: " + tbl1);
			// System.out.println("tbl2 :: " + tbl2);
			 
			 

			columnOrderMappingJoin = new HashMap<>();
			getColumnOrderMappingForJoin(tbl1, tbl2, false);

			// System.out.println("columnOrderMappingJoin :: " +
			// columnOrderMappingJoin);

			File file;
			file = new File("data/" + tbl1 + ".csv");

			String row;
			BufferedReader br = new BufferedReader(new FileReader(file));
			int idx = columnOrderMapping.get(temp[0].trim());

			// System.out.println("idx tbl1 :: " + idx);

			//TreeMap orderIndexTbl2 = new TreeMap<>();
			HashMap orderIndexTbl2 = new HashMap<>();
			orderIndexTbl2 = (HashMap) columnIndex.get(temp[1].trim());

			Boolean allOrder = false;
			String keyCol = null;
			String newTbl = null;

			while ((row = br.readLine()) != null) {

				results = new HashMap<>();
				for (String theJoinCondition : joinOnClause) {

					results.put(theJoinCondition, new ArrayList<>());
				}
				orderIndexTbl2 = new HashMap<>();
				orderIndexTbl2 = (HashMap) columnIndex.get(base[1].trim());

				joinedTables = new ArrayList<>();
				joinedTables.add(tt1);
				joinedTables.add(tt2);

				tbl1 = tt1;
				tbl2 = tt2;

				String[] valuesTemp = row.split("\\|");
				String[] base1 = row.split("\\|");
				String key = valuesTemp[idx];
				
				Boolean exec = true;
				if(joinOnClause.size() == 3){
					if(valuesTemp[8].equals("R")){
						exec = true;
					}else{
						exec = false;
					}
				}

				if(exec){
				if (orderIndexTbl2.containsKey(Integer.parseInt(key))) {
					ArrayList<Long> pks = new ArrayList<>();
					pks = (ArrayList<Long>) orderIndexTbl2.get(Integer.parseInt(key));

					// System.out.println("tableMappingJoin" +
					// tableMappingJoin);
					// System.out.println("tbl2 :: " + tbl2);
					TableData td = new TableData();
					td = tableMappingJoin.get(tbl2.trim());

					// System.out.println("td pk :: " +
					// td.getPrimaryKeyIndex());

					primaryKeyIndex = (Map<Long, String[]>) td.getPrimaryKeyIndex();
					// System.out.println("pks :: " + pks);

					ArrayList<String[]> inter = new ArrayList<>();
					for (Long l : pks) {

						// inter = new ArrayList<>();
						String[] values2 = tableMappingJoin.get(tbl2.trim()).primaryKeyIndex.get(l);

						if (values2 != null) {
							// System.out.println("values2 :: " +
							// Arrays.toString(values2));

							List<String> list = new ArrayList<String>();

							List<String> tempList = new ArrayList<String>();
							List<String> tempList2 = new ArrayList<String>();
							for (String v : valuesTemp) {
								if (v != null) {
									tempList.add(v);
								}
							}
							for (String v : values2) {
								if (v != null) {
									tempList2.add(v);
								}
							}

							list = new ArrayList<String>(tempList);
							list.addAll(tempList2);

							// System.out.println("list :: " + list);
							// System.out.println("list :: " + list);
							// values = new String[list.size()];
							String[] base2 = new String[list.size()];
							line = false;

							int x = 0;
							for (int t = 0; t < list.size() && (list.get(t) != null); t++) {
								// values[x++] = list.get(t);
								base2[x++] = list.get(t);

							}
							inter.add(base2);
						}
					}

					if (inter.size() != 0) {
						results.put(joinOnClause.get(0), inter);
					}
					// System.out.println(results);

					// check for more join conditions

					/*
					 * if (joinOnClause.size() <= 1) { PrimitiveValue ret =
					 * null; processReadFromFile(ret); }
					 */

					for (int j = 1; j < joinOnClause.size(); j++) {

						// System.out.println("joinOnClause :: " +
						// joinOnClause.get(j));
						// System.out.println("joinedTables :: " +
						// joinedTables);

						temp = joinOnClause.get(j).split("=");
						// temp2 = temp[0].split("\\.");

						tbl1 = temp[0].split("\\.")[0].trim();
						tbl2 = temp[1].split("\\.")[0].trim();

						// System.out.println("more join :: " + tbl1 +
						// "\t" + tbl2);

						// if (allOrder == false) {
						if (joinedTables.contains(tbl1) && !joinedTables.contains(tbl2)) {
							if (allOrder == false) {
								getColumnOrderMappingForJoin(tbl1, tbl2, true);
							}
							joinedTables.add(tbl2);
							newTbl = temp[1];
							keyCol = temp[0].trim();
						} else if (joinedTables.contains(tbl2) && !joinedTables.contains(tbl1)) {
							if (allOrder == false) {
								getColumnOrderMappingForJoin(tbl2, tbl1, true);
							}
							joinedTables.add(tbl1);
							newTbl = temp[0];
							keyCol = temp[1].trim();
						} else {
							newTbl = null;
						}
						// }

						// System.out.println("newTbl :: " + newTbl);

						// get hashmap of the new table
						if (newTbl != null) {
							orderIndexTbl2 = new HashMap<>();
							orderIndexTbl2 = (HashMap) columnIndex.get(newTbl.trim());

							ArrayList<String[]> tempRes;
							if (results.get(joinOnClause.get(j - 1)).size() != 0) {
								tempRes = results.get(joinOnClause.get(j - 1));
							} else {
								tempRes = results.get(joinOnClause.get(j - 2));
							}

							/*
							 * System.out.println("columnOrderMappingJoin ::" +
							 * columnOrderMappingJoin);
							 * System.out.println("keyCol :: " + keyCol);
							 * System.out.println("values :: " +
							 * Arrays.toString(values));
							 */

							for (String[] tr : tempRes) {

								key = tr[columnOrderMappingJoin.get(keyCol)];

								ArrayList<String> tempList;
								ArrayList<String> tempList2;
								ArrayList<String> list;
								if (orderIndexTbl2.containsKey(Integer.parseInt(key))) {

									ArrayList<Long> pks2 = new ArrayList<>();

									pks2 = (ArrayList<Long>) orderIndexTbl2.get(Integer.parseInt(key));
									td = new TableData();
									td = tableMappingJoin.get(newTbl.split("\\.")[0].trim());

									primaryKeyIndex = (Map<Long, String[]>) td.getPrimaryKeyIndex();

									inter = new ArrayList<>();
									for (Long nl : pks2) {

										values2 = tableMappingJoin.get(newTbl.split("\\.")[0].trim()).primaryKeyIndex
												.get(nl);
										tempList = new ArrayList<String>();
										tempList2 = new ArrayList<String>();
										for (String v : tr) {
											if (v != null) {
												tempList.add(v);
											}
										}
										for (String v : values2) {
											if (v != null) {
												tempList2.add(v);
											}
										}

										list = new ArrayList<String>(tempList);
										list.addAll(tempList2);

										// values = new String[list.size()];
										String[] base3 = new String[list.size()];
										line = false;

										int x = 0;
										for (int t = 0; t < list.size() && (list.get(t) != null); t++) {
											// values[x++] = list.get(t);
											base3[x++] = list.get(t);

										}

										inter.add(base3);
									}
									if (results.get(joinOnClause.get(j)).size() > 0) {

										ArrayList<String[]> appendTo = results.get(joinOnClause.get(j));
										appendTo.addAll(inter);
										results.put(joinOnClause.get(j), appendTo);
									} else {
										results.put(joinOnClause.get(j), inter);
									}

								}

							}
						}

						// System.out.println(Arrays.toString(values));

						// pass this new result row for further processing
						// PrimitiveValue ret = null;
						// processReadFromFile(ret);
					}
					allOrder = true;
					// System.out.println(results);

					ArrayList<String[]> process = results.get(joinOnClause.get(joinOnClause.size() - 1));
					for (String[] p : process) {
						// System.out.println(Arrays.toString(s));
						values = p;
						PrimitiveValue ret = null;
						processReadFromFile(ret);
					}
				}
				}
			}
			if (numAggFunc > 0)
				printAggregateResult();
		} else {

			/*
			 * read from the file directly, as no order by clause is present
			 */
			if (orderOperator == false || groupByOperator == true) {

				File file;
				file = new File("data/" + Main.myTableName + ".csv");

				// get the where clause
				e = plainSelect.getWhere();

				// if (e != null) {
				// if (e.toString().contains("LINEITEM.QUANTITY")) {
				// file = new File("data/LINEITEM.QUANTITY.csv");
				// }
				// }
				BufferedReader br = new BufferedReader(new FileReader(file));

				// reinitializeValues();

				PrimitiveValue ret = null;

				try {

					while ((newRow = br.readLine()) != null) {

						line = true;
						// if(stop == false){
						processReadFromFile(ret);
						// }else{
						// //System.out.println("stopped");
						// break;
						// }
					}

					/*
					 * done with file reading...if aggregate function, then
					 * print after this and not in printToConsole
					 */

					if (numAggFunc > 0)
						printAggregateResult();

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}

			} else {// order by present, read from the maps created
				line = false;
				e = plainSelect.getWhere();
				reinitializeValues();
				PrimitiveValue ret = null;

				// firstOrderOperator csv file name
				String firstOrderOperator = orderByElementsList.get(0);

				// start of in-mem
				if (inmem) {
					TreeMap orderIndexMap = new TreeMap<>();
					primaryKeyIndex = tableData.getPrimaryKeyIndex();
					// System.out.println(firstOrderOperator);
					String[] temp12 = firstOrderOperator.split("\\.");
					String searchOnIndex = (temp12.length == 1) ? temp12[0] : temp12[1];

					// System.out.println(Arrays.toString(temp12));

					boolean m = false;

					for (Entry<String, Map> ci : columnIndex.entrySet()) {
						if (ci.getKey().contains(searchOnIndex.trim())) {
							// System.out.println("index present");
							orderIndexMap = (TreeMap) ci.getValue();
							m = true;
							break;
						}
					}

					if (m == false) {
						// if index not built on order by column, build it on
						// the
						// fly
						MyCreateTable.sortMyTable(firstOrderOperator, tableData.getPrimaryKeyList(), true);
						orderIndexMap = (TreeMap) columnIndex.get(firstOrderOperator);
					}

					// iterate through sorted hashmap to fetch rows

					Iterator iterator = null;
					if (orderByElementsSortOrder.get(firstOrderOperator) == 1) {
						iterator = orderIndexMap.entrySet().iterator();
					} else {
						iterator = orderIndexMap.descendingMap().entrySet().iterator();
					}

					while (iterator.hasNext()) {

						if (isDone) {
							break;
						}

						Map.Entry entry = (Entry) iterator.next();

						// System.out.println("--" + entry);

						// if multiple rows have same index value(clustered)
						List<Long> toOrderByElement2 = (List<Long>) entry.getValue();

						if (toOrderByElement2.size() > 1 && orderByElementsList.size() > 1) {
							// 2 order by column criteria, then sort the cluster
							// based
							// on second column
							toOrderByElement2 = MyCreateTable.sortOnIndex2(orderByElementsList.get(1),
									toOrderByElement2);

							// print cluster in rev when desc(desc = 2)
							if (orderByElementsSortOrder.get(orderByElementsList.get(1)) == 2) {
								for (int i = toOrderByElement2.size() - 1; i >= 0; i--) {
									values = primaryKeyIndex.get(toOrderByElement2.get(i));
									processReadFromFile(ret);
								}

							} else {
								for (Long rowString : toOrderByElement2) {
									// read new row from (PK,entire row ) map
									values = primaryKeyIndex.get(rowString);
									processReadFromFile(ret);
								}
							}
						} else {

							// clustered index
							for (Long rowString : toOrderByElement2) {
								// read new row from (PK,entire row ) map
								values = primaryKeyIndex.get(rowString);
								processReadFromFile(ret);
							}
						}

					}

					if (numAggFunc > 0)
						printAggregateResult();
				} else {

					// ---------------------------onDisk---------------------------

					// System.out.println("file:" + firstOrderOperator);
					File file;
					file = new File("data/" + firstOrderOperator + ".csv");

					BufferedReader br = new BufferedReader(new FileReader(file));
					// get the where clause
					e = plainSelect.getWhere();

					// reinitializeValues();

					ret = null;

					try {

						while ((newRow = br.readLine()) != null) {

							if (isDone) {
								// System.out.println("done dona done done!");
								break;
							}

							line = true;
							processReadFromFile(ret);
						}

						/*
						 * done with file reading...if aggregate function, then
						 * print after this and not in printToConsole
						 */

						if (numAggFunc > 0)
							printAggregateResult();

					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}

				}
			}
		}
	}

	private static void getColumnOrderMappingForJoin(String tbl1, String tbl2, Boolean isOld) {
		// TODO Auto-generated method stub

		tbl1 = tbl1.trim();
		tbl2 = tbl2.trim();
		int s = 0;
		for (Entry<String, Integer> c : columnOrderMapping.entrySet()) {

			if (c.getKey().contains(tbl1)) {
				s++;
			}

		}

		if (columnOrderMappingJoin != null && columnOrderMappingJoin.size() != 0) {
			s = columnOrderMappingJoin.size();
		}
		// System.out.println("map join :: " + columnOrderMappingJoin);
		// System.out.println("size :: -- " + s);

		for (Entry<String, Integer> c : columnOrderMapping.entrySet()) {

			String[] temp = c.toString().split("\\.");
			String ttbl = temp[0].trim();

			if (isOld == false) {
				if (ttbl.equals(tbl1)) {
					columnOrderMappingJoin.put(c.getKey(), c.getValue());
				} else if (c.getKey().contains(tbl2)) {
					columnOrderMappingJoin.put(c.getKey(), c.getValue() + s);
				}
			} else {

				if (ttbl.equals(tbl2)) {
					columnOrderMappingJoin.put(c.getKey(), c.getValue() + s);
				}

			}

		}

		// System.out.println("1....columnOrderMappingJoin :: " +
		// columnOrderMappingJoin);
	}

	private static void processReadFromFileForJoin(String newRow1, String newRow2)
			throws InvalidPrimitive, SQLException {
		// TODO Auto-generated method stub

		values = newRow1.split("\\|", -1);
		values2 = newRow2.split("\\|", -1);

		// where evaluation
		if (!(e == null)) {

			if (eval.eval(e).toBool()) {
				printToConsoleForJoin(newRow1, newRow2);
			}

		}
	}

	private static void printToConsoleForJoin(String newRow1, String newRow2) {
		// TODO Auto-generated method stub

		System.out.println(newRow1 + " |||| " + newRow2);

	}

	public static void processReadFromFile(PrimitiveValue ret) throws SQLException {

		if (innerSelects.size() != 0) {
			outermost = false;
		}

		/* read line from csv file */
		/* values array have individual column values from the file */

		// System.out.println();
		if (line) {
			values = newRow.split("\\|", -1);
		}

		/* where clause evaluation */
		if (!(e == null)) {

			if (eval.eval(e).toBool()) {
				if (numAggFunc > 0) {
					computeAggregate();
				} else {
					printToConsole();
				}
			} else {
				newRow = ""; // making this "" as it shouldn't be passed on to
				values = null; // outer selects
			}
		} else {
			if (numAggFunc > 0) {
				computeAggregate();
			} else {
				printToConsole();
			}
		}

		/*
		 * row is returned from the file, so pass it on to outer select
		 * statements
		 */
		if (!outermost) {
			if (values != null) {

				/*
				 * innerSelects has a list of all the inner/nested select
				 * statements NOT the outermost/main select statement
				 */
				for (int i = innerSelects.size() - 2; i >= 0 && (values != null); i--) {

					reinitializeValues();
					pq.processInBetweenSelect(innerSelects.get(i).toString());
				}

				/*
				 * row is returned till the end
				 */
				if (values != null) {
					outermost = true;

					// reinitializeValues();

					pq.processInBetweenSelect(query.toString());
				}
			}
		}

	}

	/*
	 * sum = 1 min = 2 max = 3 avg = 4 count = 5
	 */
	public static void printAggregateResult() {

		StringBuilder sb = new StringBuilder();
		if (groupByElementsList == null) {

			for (int i = 0; i < aggAlias.length; i++) {
				if (aggResults.get(aggAlias[i]) != null) {
					sb.append(aggResults.get(aggAlias[i]));
					sb.append('|');
				}
				if (aggNo[i] == 5 && aggResults.get(aggAlias[i]) == null) {
					sb.append(0);
					sb.append('|');
				}
			}

			if (sb.length() > 0) {
				sb.setLength(sb.length() - 1);

				// System.out.println(sb);
				
				// put in map if order operator present

				if (orderOperator == true && groupByOperator == true) {
					outputDataOD.add(sb.toString());
				} else {

					System.out.println(sb.toString());

				}
			}
		} else {

			List<String> tempList = new ArrayList<String>();
			for (Column c : groupByElementsList) {
				tempList.add(c.toString());
			}

			int[] pos = new int[tempList.size()];
			int j;

			for (j = 0; j < pos.length; j++)
				pos[j] = -1;

			for (j = 0; j < selectItemsAsObject.length; j++) {
				if (selectItemsAsObject[j] != null) {
					pos[j] = tempList.indexOf(selectItemsAsObject[j].toString());
				}
			}

			// System.out.println(Arrays.toString(pos));

			ArrayList<String> selCols = new ArrayList<>();
			ArrayList<String> aggCols = new ArrayList<>();
			int si = 0, ai = 0;
			// get the sel columns
			for (Entry<String, HashMap<String, Double>> a : aggGroupByMap.entrySet()) {
				sb = new StringBuilder();
				String[] sitems = a.getKey().split(":");
				// System.out.println(Arrays.toString(sitems));

				// ignore the 0th index
				for (j = 0; j < pos.length && pos[j] != -1; j++) {
					if (pos.length == 1) {
						// sb.append(sitems[pos[j]]);
						selCols.add(sitems[pos[j]]);
					} else {
						// sb.append(sitems[pos[j] + 1]);
						selCols.add(sitems[pos[j] + 1]);
					}
					// sb.append("|");
				}

				// now get the agg results
				HashMap<String, Double> agg = a.getValue();
				Double count = agg.get(countAlias);
				for (int i = 0; i < aggAlias.length; i++) {

					if (aggNo[i] == 4) {
						i = i + 1;
						// sb.append(agg.get(aggAlias[i]) / count);
						// sb.append('|');
						aggCols.add((agg.get(aggAlias[i]) / count) + "");
					} else {

						if (agg.get(aggAlias[i]) != null) {
							// sb.append(agg.get(aggAlias[i]));
							// sb.append('|');
							aggCols.add(agg.get(aggAlias[i]) + "");
						}
					}
					if (aggNo[i] == 5 && agg.get(aggAlias[i]) == null) {
						// sb.append(0);
						aggCols.add("0");
						// sb.append('|');
					}

				}

				if (sb.length() > 0)
					sb.setLength(sb.length() - 1);

				StringBuilder sb1 = new StringBuilder();

				for (SelectItem sitem : plainSelect.getSelectItems()) {
					Expression selExpr = ((SelectExpressionItem) sitem).getExpression();

					if (selExpr instanceof Function) {
						sb1.append(aggCols.get(ai));
						sb1.append("|");
						ai += 1;
					} else {
						sb1.append(selCols.get(si));
						sb1.append("|");
						si += 1;
					}
				}

				if (sb1.length() > 0)
					sb1.setLength(sb1.length() - 1);

				// System.out.println(sb);
				// if (inmem) {
				// System.out.println(sb);
				// }
				

				// put in map if ondisk & order operator present

				if (orderOperator == true && groupByOperator == true) {
					//System.out.println("in agg :: " + outputDataOD);
					outputDataOD.add(sb1.toString());
				} else {

					System.out.println(sb1.toString());

				}
			}

		}
	}

	/*
	 * sum = 1 min = 2 max = 3 avg = 4 count = 5
	 */

	public static void computeAggregate() throws SQLException {

		print = false;
		aggPrint = true;

		if (isJoin) {
			colMap = columnOrderMappingJoin;
		} else {
			colMap = columnOrderMapping;
		}
		if (groupByElementsList != null) {

			String key = "";
			if (groupByElementsList.size() == 1) {
				int idx = colMap.get(groupByElementsList.get(0).toString());
				key = values[idx];
			} else {
				for (int i = 0; i < groupByElementsList.size(); i++) {
					int idx = colMap.get(groupByElementsList.get(i).toString());
					key = key + ":" + values[idx];
				}
			}

			for (int i = 0; i < numAggFunc; i++) {

				if (aggNo[i] == 5) { // count

					if (groupByElementsList.size() != 0) {

						if (!aggGroupByMap.containsKey(key)) {

							HashMap<String, Double> agg = new HashMap<>();
							agg.put(aggAlias[i], 1.0);

							aggGroupByMap.put(key, agg);

						} else {
							HashMap<String, Double> agg = aggGroupByMap.get(key);
							if (!agg.containsKey(aggAlias[i])) {
								// System.out.println("--count--");
								agg.put(aggAlias[i], 1.0);
							} else {
								Double c = agg.get(aggAlias[i]);
								c = c + 1;
								agg.put(aggAlias[i], c);
							}

							aggGroupByMap.put(key, agg);
						}

					}
				}

				if (aggNo[i] != 5) {
					aggExpr = (Expression) aggExprs[i];
					answer = computeExpression();

					if (aggNo[i] == 1) {

						if (groupByElementsList.size() != 0) {

							if (!aggGroupByMap.containsKey(key)) {

								HashMap<String, Double> agg = new HashMap<>();
								agg.put(aggAlias[i], answer.toDouble());

								aggGroupByMap.put(key, agg);

							} else {

								HashMap<String, Double> agg = aggGroupByMap.get(key);

								if (!agg.containsKey(aggAlias[i])) {
									agg.put(aggAlias[i], answer.toDouble());
								} else {
									Double sum = agg.get(aggAlias[i]) + answer.toDouble();
									agg.put(aggAlias[i], sum);
								}

								aggGroupByMap.put(key, agg);
							}

						}

					} else if (aggNo[i] == 2) { // min

						if (groupByElementsList.size() != 0) {

							if (!aggGroupByMap.containsKey(key)) {

								HashMap<String, Double> agg = new HashMap<>();
								agg.put(aggAlias[i], answer.toDouble());

								aggGroupByMap.put(key, agg);

							} else {

								HashMap<String, Double> agg = aggGroupByMap.get(key);

								if (!agg.containsKey(aggAlias[i])) {
									agg.put(aggAlias[i], answer.toDouble());
								} else {
									Double min = agg.get(aggAlias[i]);
									if (answer.toDouble() < min) {
										min = answer.toDouble();
										agg.put(aggAlias[i], min);
									}

								}

								aggGroupByMap.put(key, agg);
							}

						}

					} else if (aggNo[i] == 3) {

						if (groupByElementsList.size() != 0) {

							if (!aggGroupByMap.containsKey(key)) {

								HashMap<String, Double> agg = new HashMap<>();
								agg.put(aggAlias[i], answer.toDouble());

								aggGroupByMap.put(key, agg);

							} else {

								HashMap<String, Double> agg = aggGroupByMap.get(key);

								if (!agg.containsKey(aggAlias[i])) {
									agg.put(aggAlias[i], answer.toDouble());
								} else {
									Double max = agg.get(aggAlias[i]);
									if (answer.toDouble() > max) {
										max = answer.toDouble();
										agg.put(aggAlias[i], max);
									}

								}

								aggGroupByMap.put(key, agg);
							}

						}

					} else if (aggNo[i] == 4) {

						if (!aggGroupByMap.containsKey(key)) {

							HashMap<String, Double> agg = new HashMap<>();
							agg.put(aggAlias[i], answer.toDouble());

							aggGroupByMap.put(key, agg);

						} else {

							HashMap<String, Double> agg = aggGroupByMap.get(key);

							avgCount++;
							avgTotal += answer.toDouble();
							agg.put(aggAlias[i], avgTotal / avgCount);

							aggGroupByMap.put(key, agg);
						}

					}
				}
				// if avg
				// if (aggNo[i] == 4) {
				//
				// Double[] arr = aggGroupByMap.get(key);
				// arr[3] = arr[0] / arr[4];
				// aggGroupByMap.put(key, arr);
				// }

			}
		} else {

			print = false;
			aggPrint = true;

			for (int i = 0; i < numAggFunc; i++) {
				if (aggNo[i] == 5) {
					if (!aggResults.containsKey(aggAlias[i])) {
						// System.out.println("--count--");
						aggResults.put(aggAlias[i], 1.0);
					} else {
						Double c = aggResults.get(aggAlias[i]);
						c = c + 1;
						aggResults.put(aggAlias[i], c);
					}

				} else {
					aggExpr = (Expression) aggExprs[i];
					answer = computeExpression();

					if (aggNo[i] == 1) {

						if (!aggResults.containsKey(aggAlias[i])) {
							aggResults.put(aggAlias[i], answer.toDouble());
						} else {
							Double sum = aggResults.get(aggAlias[i]) + answer.toDouble();
							aggResults.put(aggAlias[i], sum);
						}

					} else if (aggNo[i] == 2) {

						if (!aggResults.containsKey(aggAlias[i])) {
							aggResults.put(aggAlias[i], answer.toDouble());
						} else {
							Double min = aggResults.get(aggAlias[i]);
							if (answer.toDouble() < min) {
								min = answer.toDouble();
								aggResults.put(aggAlias[i], min);
							}

						}

					} else if (aggNo[i] == 3) {

						if (!aggResults.containsKey(aggAlias[i])) {
							aggResults.put(aggAlias[i], answer.toDouble());
						} else {
							Double max = aggResults.get(aggAlias[i]);
							if (answer.toDouble() > max) {
								max = answer.toDouble();
								aggResults.put(aggAlias[i], max);
							}

						}

					} else if (aggNo[i] == 4) {
						avgCount++;
						avgTotal += answer.toDouble();
						aggResults.put(aggAlias[i], avgTotal / avgCount);
					}
				}

			}
		}

	}

	public static void printToConsole() throws SQLException {

		if (selectStar == true) {

			if (outermost && ((limit >= 1 && count < limit) || limit == -1)) {
				if (values != null) {
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < values.length; i++) {
						if (values[i] != null) {
							sb.append(values[i]);
							sb.append("|");
						} else {
							break;
						}
					}
					if (sb.length() > 0)
						sb.setLength(sb.length() - 1);

					// System.out.println(sb);
					if (inmem) {
						// System.out.println("conut: " + count);
						System.out.println(sb);
					}

					// put in map if ondisk & order operator present

					if (orderOperator == true && groupByOperator == true) {
						outputDataOD.add(sb.toString());
					} else {

						System.out.println(sb.toString());

					}
					count++;
				}
				if (innerSelects.size() != 0) {
					outermost = false;
				}

			}
			if (count >= limit && limit != -1) {
				// System.out.println("------------");
				isDone = true;
			}
		} else {

			if (isJoin) {
				colMap = columnOrderMappingJoin;
			} else {
				colMap = columnOrderMapping;
			}
			sbuilder = new StringBuilder();
			for (int i = 0; i < selCols; i++) {
				SelectExpressionItem sitem = (SelectExpressionItem) selectItemsAsObject[i];

				if (selExp instanceof Addition || selExp instanceof Subtraction || selExp instanceof Multiplication
						|| selExp instanceof Division) {

					Eval eval = new Eval() {
						public PrimitiveValue eval(Column c) {

							int idx = colMap.get(c.toString());
							String ptype = columnDataTypeMapping.get(c.toString());
							return getReturnType(SQLDataType.valueOf(ptype), values[idx]);
						}
					};

					result = eval.eval(selExp);
					if (result != null) {
						sbuilder.append(result);
					}

				} else {
					int idx = colMap.get(sitem.toString());
					if (values[idx] != null) {
						sbuilder.append(values[idx]);
					}
				}

				if (i != selCols - 1)
					sbuilder.append("|");
			}

			if (outermost && ((limit >= 1 && count < limit) || limit == -1)) {
				if (values != null) {
					// System.out.println(sbuilder.toString());
					if (inmem) {
						System.out.println(sbuilder);
					}

					// put in map if ondisk & order operator present

					if (orderOperator == true && groupByOperator == true) {
						outputDataOD.add(sbuilder.toString());
					} else {

						System.out.println(sbuilder.toString());

					}
					count++;
				}
				if (innerSelects.size() != 0) {
					outermost = false;
				}

			} else {
				newRow = sbuilder.toString();
			}
			if (count >= limit && limit != -1) {
				isDone = true;
			}
		}

	}

	private static void finalPrintforOrderBy() {
		// TODO Auto-generated method stub

		//System.out.println("outputDataOD" + outputDataOD);
		
		List<SelectItem> selectList = plainSelect.getSelectItems();

		if (plainSelect.getOrderByElements() != null) {
			orderByElementsList = new ArrayList<String>();

			for (OrderByElement o : plainSelect.getOrderByElements()) {
				if (o.isAsc()) {
					orderByElementsList.add(o.toString());
					orderByElementsSortOrder.put(o.toString(), 1);

				} else {
					String orderByElement[] = o.toString().split(" ");
					orderByElementsList.add(orderByElement[0]);
					orderByElementsSortOrder.put(orderByElement[0], 2);
				}
			}

			orderOperator = true; /*
									 * tells us from where to read
									 * the data::file or map
									 */
		}
		
		//System.out.println("orderByElementsList :: " + orderByElementsList);
		
		String firstOperator = orderByElementsList.get(0);
		
		//System.out.println("firstOperator :: " + firstOperator);
		int firstOperatorIndex = 0, i = 0;
		for (SelectItem s : selectList) {
			if (s.toString().contains(firstOperator)) {
				firstOperatorIndex = i;
				break;
			}
			i++;
		}

		String secondOperator = "";
		i = 0;
		secondOperatorIndex = 0;
		if (orderByElementsList.size() > 1) {
			secondOperator = orderByElementsList.get(1);

			for (SelectItem s : selectList) {
				if (s.toString().contains(secondOperator)) {
					secondOperatorIndex = i;
					break;
				}
				i++;
			}
		}

		int order = orderByElementsSortOrder.get(firstOperator);
		// 1 is ascending

		if (orderOperator == true && groupByOperator == true) {
			// order by second column

			TreeMap outputDataODMap = new TreeMap<>();
			List<String> list = new ArrayList<String>();
			for (String rowVal : outputDataOD) {
				String opVal[] = rowVal.split("\\|");

				if (opVal[firstOperatorIndex].matches("[-+]?\\d*\\.?\\d+")) {
					Double key = Double.parseDouble(opVal[firstOperatorIndex]);

					if (outputDataODMap.containsKey(key)) {
						list = (List<String>) outputDataODMap.get(key);
						list.add(rowVal);

						if (orderByElementsList.size() > 1) {
							Collections.sort(list, new Comparator<String>() {
								public int compare(String a1, String a2) {
									String a1Arr[] = a1.split("\\|");
									String a2Arr[] = a2.split("\\|");

									if (a1Arr[secondOperatorIndex].matches("[-+]?\\d*\\.?\\d+")) {
										double d1 = Double.parseDouble(a1Arr[secondOperatorIndex]);
										double d2 = Double.parseDouble(a1Arr[secondOperatorIndex]);

										return Double.compare(d1, d2);
									}
									return a1Arr[secondOperatorIndex].compareTo(a2Arr[secondOperatorIndex]);
								}
							});
						}
						outputDataODMap.put(key, list);

					} else {
						list = new ArrayList<String>();
						list.add(rowVal);
						outputDataODMap.put(key, list);
					}

				} else {
					// key is string

					if (outputDataODMap.containsKey(opVal[firstOperatorIndex])) {
						list = (List<String>) outputDataODMap.get(opVal[firstOperatorIndex]);
						list.add(rowVal);

						if (orderByElementsList.size() > 1) {
							Collections.sort(list, new Comparator<String>() {
								public int compare(String a1, String a2) {
									String a1Arr[] = a1.split("\\|");
									String a2Arr[] = a2.split("\\|");

									if (a1Arr[secondOperatorIndex].matches("[-+]?\\d*\\.?\\d+")) {
										double d1 = Double.parseDouble(a1Arr[secondOperatorIndex]);
										double d2 = Double.parseDouble(a1Arr[secondOperatorIndex]);

										return Double.compare(d1, d2);
									}
									return a1Arr[secondOperatorIndex].compareTo(a2Arr[secondOperatorIndex]);
								}
							});

						}

						outputDataODMap.put(opVal[firstOperatorIndex], list);

					} else {
						list = new ArrayList<String>();
						list.add(rowVal);
						outputDataODMap.put(opVal[firstOperatorIndex], list);
					}

				}

			}

			// Iterator iterator = outputDataODMap.entrySet().iterator();

			Iterator iterator = null;
			if (order == 1) {
				iterator = outputDataODMap.entrySet().iterator();
			} else {
				iterator = outputDataODMap.descendingMap().entrySet().iterator();
			}

			// System.out.println("printing");
			while (iterator.hasNext()) {
				Map.Entry entry = (Entry) iterator.next();
				for (String rowString : (ArrayList<String>) entry.getValue()) {

					if (((limit >= 1 && count < limit) || limit == -1))
						System.out.println(rowString);
					count++;

				}

			}
		}

	}

	public static Eval eval = new Eval() {
		public PrimitiveValue eval(Column c) {
			// isJoin = false;
			if (isJoin) {

				// System.out.println("c :: " + c);
				String[] temp = c.toString().split("\\.");
				String tbl = temp[0];
				// System.out.println("tbl :: " + tbl);

				// System.out.println("columnOrderMappingJoin :: "
				// +columnOrderMappingJoin);
				int idx;
				String ptype;

				if (columnOrderMappingJoin != null && c != null) {
					idx = columnOrderMappingJoin.get(c.toString());
					ptype = columnDataTypeMapping.get(c.toString());
				} else {
					idx = columnOrderMapping.get(c.toString());
					ptype = columnDataTypeMapping.get(c.toString());
				}
				// System.out.println("values :: " + Arrays.toString(values));
				// System.out.println("idx :: " + idx);
				// System.out.println("idx :: " + idx);

				return getReturnType(SQLDataType.valueOf(ptype), values[idx]);

			} else {
				// System.out.println("c :: " + c);
				// System.out.println("mytblname :: " + myTableName);
				// if (c.toString().contains(myTableName)) {
				Main.tableData = Main.tableMapping.get(myTableName);

				Main.columnOrderMapping = Main.tableData.getColumnOrderMapping();
				Main.columnDataTypeMapping = Main.tableData.getColumnDataTypeMapping();
				/*
				 * } else { Main.tableData = Main.tableMapping.get(alias);
				 * 
				 * Main.columnOrderMapping =
				 * Main.tableData.getColumnOrderMapping();
				 * Main.columnDataTypeMapping =
				 * Main.tableData.getColumnDataTypeMapping(); }
				 */

				int idx = columnOrderMapping.get(c.toString());
				String ptype = columnDataTypeMapping.get(c.toString());

				// return getReturnType(ptype, values[idx]);
				// System.out.println(Arrays.toString(values));

				return getReturnType(SQLDataType.valueOf(ptype), values[idx]);
			}
		}
	};

	static PrimitiveValue expResult = null;

	private static PrimitiveValue computeExpression() throws SQLException {
		expResult = eval.eval(aggExpr);
		return expResult;
	}

	public static ArrayList<Expression> extractAllExp(Expression expression) {
		ArrayList<Expression> hashSet = new ArrayList<Expression>();

		Expression leftVal = null;
		Expression rightVal = null;

		if (expression instanceof AndExpression) {
			AndExpression mte = (AndExpression) expression;
			leftVal = ((Expression) mte.getLeftExpression());
			rightVal = ((Expression) mte.getRightExpression());

			if (leftVal instanceof AndExpression || leftVal instanceof OrExpression) {
				ArrayList<Expression> array = extractAllExp(leftVal);
				for (Expression s : array) {
					hashSet.add(s);
				}
				hashSet.add(rightVal);
			} else {

				// if (leftVal instanceof EqualsTo) {
				hashSet.add(leftVal);
				// }
				// if (rightVal instanceof EqualsTo) {
				hashSet.add(rightVal);
				// }
			}

		} else if (expression instanceof OrExpression) {
			OrExpression mte = (OrExpression) expression;
			leftVal = ((Expression) mte.getLeftExpression());
			rightVal = ((Expression) mte.getRightExpression());
			;

			if (leftVal instanceof AndExpression || leftVal instanceof OrExpression) {
				ArrayList<Expression> array = extractAllExp(leftVal);
				for (Expression s : array) {
					hashSet.add(s);
				}
				hashSet.add(rightVal);
			} else {
				// if (leftVal instanceof EqualsTo) {
				hashSet.add(leftVal);
				// }
				// if (rightVal instanceof EqualsTo) {
				hashSet.add(rightVal);
				// }
			}

		} else {
			hashSet.add(expression);
		}
		return hashSet;
	}

	public static ArrayList<String> evaluateJoinCondition(TableData table1, TableData table2, Expression expression) {
		ArrayList<String> arrayList = new ArrayList<String>();

		ArrayList<String> set = extractCond(expression);

		for (String s : set) {
			String[] strArr = null;
			if (s.contains("=")) {
				// System.out.println("one expr " + s);
				String joincols[] = s.split("=");

				// System.out.println(Arrays.toString(joincols));
				String col1 = joincols[0];
				String col2 = joincols[1];
				col1 = col1.trim();
				col2 = col2.trim();
				// System.out.println("col1:" + col1 + "col2" + col2);
				// System.out.println(columnOrderMapping);
				if (columnOrderMapping.containsKey(col1) && columnOrderMapping.containsKey(col2)) {
					arrayList.add(s);

				}
			}
		}

		System.out.println(arrayList);

		return arrayList;
	}

	public static ArrayList<String> extractCond(Expression expression) {
		// System.out.println(expression.toString()+"\n\n");
		ArrayList<String> hashSet = new ArrayList<String>();
		ArrayList<Expression> hashExpSet = extractAllExp(expression);
		for (Expression exp : hashExpSet) {
			hashSet.add(exp.toString());
		}
		// System.out.println(hashSet);
		return hashSet;
	}
}

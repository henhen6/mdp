{
  "title": { "table": "#(table.buildEntityClassName())" },
#for(column : table.allColumns)
  "#(column.property)": "#(firstCharToLowerCase(column.property))"#if(!for.last),#end
#end
}

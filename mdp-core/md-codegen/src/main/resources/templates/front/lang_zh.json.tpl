{
  "title": { "table": "#(table.getComment())" },
#for(column : table.allColumns)
  "#(column.property)": "#(column.getSwaggerComment())"#if(!for.last),#end
#end
}

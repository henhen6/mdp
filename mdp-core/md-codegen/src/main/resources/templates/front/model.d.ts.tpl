export namespace #(table.buildEntityClassName())Type {
  export interface #(dtoClassName) {
  #for(column : table.allColumns)
    #(column.property): #(column.tsType); // #(column.getSwaggerComment())
  #end
  }

  export interface #(voClassName) {
  #for(column : table.allColumns)
    #(column.property): #(column.tsType); // #(column.getSwaggerComment())
  #end
  }

  export interface #(queryClassName) {
  #for(column : table.allColumns)
    #(column.property): #(column.tsType); // #(column.getSwaggerComment())
  #end
  }
}
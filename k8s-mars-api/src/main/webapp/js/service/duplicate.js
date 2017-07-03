'use strict'

angular.module('mainApp')
.service('duplicate',function(){

	//在list数组，更改list[id].name的值为str，检查str在list中是否重复
	var listModify = function(list,name,id,str){
		var double = false;
		if(list.length){
			list.forEach(function(item,index){
				if(index != id){
					if(item[name] == str)
						double = true;
				}
			})
		}
		return double;
	}

	//在list数组，未list添加一项值，其中name属性为str，检查str在list中是否重复
	var listAdd = function(list,name,str){
		var double = false;
		if(list.length){
			list.forEach(function(item,index){
				if(item[name] == str)
					double = true;
			});
		}
		return double;
	}

	//检查list中的每一项的[objname]数组中除本身外的每一个对象中[valname]值是否与str相同
	var containerListModify = function(list,objname,valname,id,str){
		var double = false;
		if(list.length){
			list.forEach(function(item,objindex){
				if(objindex != id){
					if(item[objname].length){
						item[objname].forEach(function(item,valindex){
							if(item[valname] == str)
								double = true;
						});
					}
				}
			});
		}
		return double;
	}

	var containerListAdd = function(list,objname,valname,str){
		var double = false;
		if(list.length){
			list.forEach(function(item,objindex){
				if(item[objname].length){
					item[objname].forEach(function(item,valindex){
						if(item[valname] == str)
							double = true;
					});
				}
			});
		}
		return double;
	}



	return {
		listModify : listModify,
		listAdd : listAdd,
		containerListModify : containerListModify,
		containerListAdd : containerListAdd
	}
})
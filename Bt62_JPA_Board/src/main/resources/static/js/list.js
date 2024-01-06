$(function (){
    $("[name='pageRows']").change(function (){
        let frm = $("[name='frmPageRwos']");
        frm.attr("method", "POST")
        frm.attr("action", "pageRows")
        frm.submit();
    });
});
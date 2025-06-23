package de.blinkt.openvpn.classforui

class SpoofDetectResult( type:String, status:Int,severity: String, title: String, message: String){
    //status:
    //0 : 시작전
    //1 : 동작중
    //2 : 완료
    private var type=type
    private var status=status
    private var severity=severity
    private var title=title
    private var message=message

    fun init(type:String, status:Int,severity: String, title: String, message: String){init(type, status,severity, title, message)}

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }
    fun setType(type:String){this.type=type}
    fun setStatus(status:Int){this.status=status}
    fun setSeverity(severity: String){this.severity=severity}

    fun getStatus(): Int {return this.status}
    fun getSeverity():String{return severity}
}
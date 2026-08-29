// Oscar Berggren
// 2026-04-27

package networking.ResponseModels

data class SendFeedbackRequest(
    val rating: Int,
    val feedback: String? = null,
)

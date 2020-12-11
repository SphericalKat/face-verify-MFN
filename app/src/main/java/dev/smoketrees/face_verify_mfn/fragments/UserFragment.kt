package dev.smoketrees.face_verify_mfn.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.afollestad.recyclical.datasource.dataSourceTypedOf
import com.afollestad.recyclical.setup
import com.afollestad.recyclical.withItem
import dagger.hilt.android.AndroidEntryPoint
import dev.smoketrees.face_verify_mfn.R
import dev.smoketrees.face_verify_mfn.activities.CameraActivity
import dev.smoketrees.face_verify_mfn.databinding.FragmentUserBinding
import dev.smoketrees.face_verify_mfn.models.UserViewHolder
import dev.smoketrees.face_verify_mfn.models.UserWithEmbeddings
import dev.smoketrees.viewmodels.AppViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
class UserFragment : Fragment() {
    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AppViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dataSource = dataSourceTypedOf<UserWithEmbeddings>()

        viewModel.getAllUsers().observe(viewLifecycleOwner, {
            dataSource.clear()
            dataSource.addAll(it)
        })

        binding.userRecyclerView.setup {
            withDataSource(dataSource)
            withItem<UserWithEmbeddings, UserViewHolder>(R.layout.user_item) {
                onBind(::UserViewHolder) { _, item ->
                    id.text = item.user.userId.toString()
                    name.text = item.user.name
                }
                
                onClick {
                    // TODO: verify user
                }
            }
        }

        binding.addUserFab.setOnClickListener {
            MaterialDialog(requireContext()).show {
                input { dialog, text ->
                    if (text.isNotEmpty()) {
                        val intent = Intent(requireContext(), CameraActivity::class.java)
                        intent.putExtra("name", text.toString())
                        startActivity(intent)
                        dialog.dismiss()
                    }
                }
            }
        }
    }
}
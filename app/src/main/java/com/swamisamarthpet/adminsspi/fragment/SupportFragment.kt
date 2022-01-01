package com.swamisamarthpet.adminsspi.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.swamisamarthpet.adminsspi.adapter.UsersAdapter
import com.swamisamarthpet.adminsspi.data.model.User
import com.swamisamarthpet.adminsspi.data.util.SupportApiState
import com.swamisamarthpet.adminsspi.databinding.FragmentSupportBinding
import com.swamisamarthpet.adminsspi.ui.SupportViewModel
import io.ktor.util.*
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@InternalAPI
class SupportFragment : Fragment() {
    private lateinit var binding: FragmentSupportBinding
    @Inject
    lateinit var usersAdapter: UsersAdapter
    private val supportViewModel:SupportViewModel by activityViewModels()
    lateinit var usersList: List<User>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding = FragmentSupportBinding.inflate(inflater,container,false)
        usersAdapter = UsersAdapter()
        binding.apply{
            usersRecyclerViewSupportFragment.apply {
                adapter = usersAdapter
                layoutManager = LinearLayoutManager(requireActivity())
            }
            supportViewModel.getAllUsers()
            handleGetAllUsersResponse()

            edtTxtSearchBarSupportFragment.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    searchUsers(s.toString())
                }

            })
        }

        return binding.root
    }

    private fun searchUsers(text: String) {
        val temp: MutableList<User> = mutableListOf()
        for (user in usersList) {
            if ((user.userName.toLowerCasePreservingASCIIRules()).contains(text.toLowerCasePreservingASCIIRules())) {
                temp.add(user)
            }
        }
        usersAdapter.submitList(temp)
    }

    private fun handleGetAllUsersResponse(){
        lifecycleScope.launchWhenStarted {
            supportViewModel.supportApiStateFlow.collect { supportApiState->
                when(supportApiState){
                    is SupportApiState.LoadingGetAllUsers ->{
                        binding.fragmentSupportProgressBar.visibility = View.VISIBLE
                    }
                    is SupportApiState.SuccessGetAllUsers ->{
                        usersList = supportApiState.data
                        usersAdapter.submitList(supportApiState.data)
                        binding.fragmentSupportProgressBar.visibility = View.GONE
                    }
                    is SupportApiState.FailureGetAllUsers ->{
                        Toast.makeText(requireContext(),"Error While loading users",Toast.LENGTH_SHORT).show()
                    }
                    is SupportApiState.EmptyGetAllUsers ->{
                    }
                    else -> {

                    }
                }
            }
        }
    }

}
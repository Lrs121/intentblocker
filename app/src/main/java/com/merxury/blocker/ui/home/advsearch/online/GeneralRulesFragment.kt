package com.merxury.blocker.ui.home.advsearch.online

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.elvishew.xlog.XLog
import com.merxury.blocker.R
import com.merxury.blocker.data.Status
import com.merxury.blocker.data.source.GeneralRuleRepository
import com.merxury.blocker.data.source.RulesRetrofitBuilder
import com.merxury.blocker.databinding.GeneralRulesFragmentBinding
import com.merxury.blocker.util.unsafeLazy

class GeneralRulesFragment : Fragment() {
    private val logger = XLog.tag("GeneralRulesFragment")
    private lateinit var viewModel: GeneralRulesViewModel
    private lateinit var binding: GeneralRulesFragmentBinding
    private val adapter by unsafeLazy { GeneralRulesAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = GeneralRuleRepository(RulesRetrofitBuilder.apiService)
        viewModel = ViewModelProvider(
            this,
            GeneralRulesViewModel.Factory(repository)
        )[GeneralRulesViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = GeneralRulesFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initSwipeLayout()
        viewModel.fetchRules().observe(viewLifecycleOwner) {
            if (it == null) {
                logger.e("rules is null")
                return@observe
            }
            when (it.status) {
                Status.SUCCESS -> {
                    logger.d("rules is success")
                    binding.swipeLayout.isRefreshing = false
                    val data = it.data
                    if (data == null) {
                        logger.e("rules is null")
                        return@observe
                    }
                    adapter.updateData(data)
                }
                Status.ERROR -> {
                    logger.e("Can't fetch rules: ${it.message}")
                    showErrorDialog(it.message)
                    binding.swipeLayout.isRefreshing = false
                }
                Status.LOADING -> {
                    logger.d("rules is loading")
                    binding.swipeLayout.isRefreshing = true
                }
            }
        }
    }

    private fun showErrorDialog(message: String?) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.oops))
            .setMessage(getString(R.string.error_occurred_with_message, message))
            .setPositiveButton(R.string.close) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            .show()
    }

    private fun initRecyclerView() {
        binding.recyclerView.adapter = adapter
    }

    private fun initSwipeLayout() {
        binding.swipeLayout.setOnRefreshListener {

        }
    }
}
package jp.co.internous.wings.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

import jp.co.internous.wings.model.domain.MstDestination;
import jp.co.internous.wings.model.mapper.MstDestinationMapper;
import jp.co.internous.wings.model.mapper.TblCartMapper;
import jp.co.internous.wings.model.mapper.TblPurchaseHistoryMapper;
import jp.co.internous.wings.model.session.LoginSession;

@Controller
@RequestMapping("/wings/settlement")
public class SettlementController {
	
	@Autowired
	private LoginSession loginSession;
	
	@Autowired
	private TblCartMapper tblCartMapper;
	
	@Autowired
	private MstDestinationMapper mstDestinationMapper;
	
	@Autowired
	private TblPurchaseHistoryMapper tblPurchaseHistoryMapper;
	
	Gson gson = new Gson();
	
	@RequestMapping("/")
	public String settlement(Model m) {
		int userId = loginSession.getUserId();		//セッションからユーザーIDを取得
		
		List<MstDestination> addressList = mstDestinationMapper.findByUserId(userId);	//住所テーブルから、ユーザーIDが一致するレコードをListに格納
		
		m.addAttribute("addressList",addressList);
		m.addAttribute("loginSession", loginSession);
			
		return "settlement";
	}
	
	/* 決算処理　決算ボタンが押されたら購入履歴テーブルにカート情報を挿入、カートテーブルからユーザーIDに紐づく情報を削除
	 * true:決済処理成功、false:決済処理失敗
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/complete")
	@ResponseBody
	public boolean complete(@RequestBody String destinationId) {
		Map<String, String> map = gson.fromJson(destinationId, Map.class);
		int id = Integer.parseInt(map.get("destinationId"));
		
		int userId = loginSession.getUserId();
		
		int countIN = tblPurchaseHistoryMapper.insert(id, userId);
		
		int countDel=0;
		if(countIN > 0) {
			countDel = tblCartMapper.deleteByUserId(userId);	// tbl_cartからuser_idが一致するレコードを削除
		}
		
		return countDel==countIN;
	}
}
